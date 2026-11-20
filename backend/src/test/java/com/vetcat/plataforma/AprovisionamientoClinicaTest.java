package com.vetcat.plataforma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.plataforma.dto.AltaClinicaRequest;
import com.vetcat.plataforma.dto.ClinicaResponse;
import com.vetcat.plataforma.repository.EsquemaClinicaRepository;
import com.vetcat.plataforma.repository.MigracionTenantRepository;
import com.vetcat.plataforma.service.AprovisionamientoClinicaService;
import com.vetcat.plataforma.service.MigradorTenant;
import com.vetcat.plataforma.service.RegistroAdministrador;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Alta de clínica de punta a punta, y que un fallo no deje nada a medias (CLI-001). */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba",
      "vetcat.migraciones.tenant=filesystem:src/test/resources/migraciones-tenant"
    })
class AprovisionamientoClinicaTest {

  private static final String ROTAS = "filesystem:src/test/resources/migraciones-tenant-rota";

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  /** Sustituye al puerto de Keycloak, que no existe hasta la fase 3. */
  @TestConfiguration
  static class AdministradorDePrueba {
    static final AtomicBoolean FALLA = new AtomicBoolean();
    static final UUID ID = UUID.randomUUID();

    @Bean
    @Primary
    RegistroAdministrador registroAdministrador() {
      return (clinicaId, nombre, correo) -> {
        if (FALLA.get()) {
          throw new IllegalStateException("Keycloak no responde");
        }
        return ID;
      };
    }
  }

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void preparaElCatalogo() throws SQLException {
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement()) {
      s.execute("CREATE SCHEMA catalogo");
    }
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();
  }

  @Autowired AprovisionamientoClinicaService servicio;
  @Autowired ClinicaRepository clinicas;
  @Autowired UsuarioClinicaRepository membresias;
  @Autowired MigracionTenantRepository estados;
  @Autowired EsquemaClinicaRepository esquemas;
  @Autowired DataSource dataSource;

  @BeforeEach
  void restableceElPuerto() {
    AdministradorDePrueba.FALLA.set(false);
  }

  private static AltaClinicaRequest alta(String nombre) {
    return new AltaClinicaRequest(
        nombre, "c@vetcat.dev", null, "America/Mexico_City", "Ana", "ana@vetcat.dev");
  }

  private String texto(String sql) throws SQLException {
    try (Connection c = dataSource.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(sql)) {
      rs.next();
      return rs.getString(1);
    }
  }

  private String cuenta(String sql) throws SQLException {
    return texto("SELECT count(*) FROM (" + sql + ") t");
  }

  @Test
  void da_de_alta_la_clinica_con_esquema_migrado_y_administrador() throws SQLException {
    ClinicaResponse clinica = servicio.aprovisionar(alta("Alta correcta"));

    assertThat(clinica.estado()).isEqualTo("ACTIVA");
    assertThat(clinica.esquema()).isEqualTo("clinica_%03d".formatted(clinica.id()));
    assertThat(texto("SELECT to_regclass('" + clinica.esquema() + ".mascota')::text"))
        .isEqualTo(clinica.esquema() + ".mascota");
    assertThat(estados.estadoDe(clinica.esquema())).contains("aplicada");
    assertThat(
            membresias.findByKeycloakIdAndClinicaIdAndEstado(
                AdministradorDePrueba.ID, clinica.id(), "ACTIVO"))
        .singleElement()
        .satisfies(m -> assertThat(m.getRol()).isEqualTo("clinica_admin"));
  }

  @Test
  void acepta_dos_clinicas_con_el_mismo_nombre() {
    ClinicaResponse a = servicio.aprovisionar(alta("Repetida"));
    ClinicaResponse b = servicio.aprovisionar(alta("Repetida"));

    assertThat(a.id()).isNotEqualTo(b.id());
    assertThat(a.esquema()).isNotEqualTo(b.esquema());
  }

  @Test
  void si_falla_el_administrador_no_queda_registro_ni_esquema() throws SQLException {
    AdministradorDePrueba.FALLA.set(true);

    assertThatThrownBy(() -> servicio.aprovisionar(alta("Se deshace")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Keycloak no responde");

    assertThat(cuenta("SELECT 1 FROM catalogo.clinica WHERE nombre = 'Se deshace'")).isEqualTo("0");
    assertThat(
            cuenta(
                "SELECT 1 FROM catalogo.migracion_tenant m LEFT JOIN catalogo.clinica c"
                    + " ON c.esquema = m.esquema WHERE c.id IS NULL"))
        .isEqualTo("0");
    assertThat(
            cuenta(
                "SELECT 1 FROM information_schema.schemata WHERE schema_name LIKE 'clinica\\_%'"
                    + " AND schema_name NOT IN (SELECT esquema FROM catalogo.clinica)"))
        .isEqualTo("0");
  }

  @Test
  void si_falla_la_migracion_no_queda_registro_ni_esquema() throws SQLException {
    var servicioConMigracionRota =
        new AprovisionamientoClinicaService(
            clinicas,
            membresias,
            estados,
            esquemas,
            new MigradorTenant(dataSource, estados, ROTAS),
            (clinicaId, nombre, correo) -> AdministradorDePrueba.ID);

    assertThatThrownBy(() -> servicioConMigracionRota.aprovisionar(alta("Migracion rota")))
        .isInstanceOf(RuntimeException.class);

    assertThat(cuenta("SELECT 1 FROM catalogo.clinica WHERE nombre = 'Migracion rota'"))
        .isEqualTo("0");
    assertThat(
            cuenta(
                "SELECT 1 FROM information_schema.schemata WHERE schema_name LIKE 'clinica\\_%'"
                    + " AND schema_name NOT IN (SELECT esquema FROM catalogo.clinica)"))
        .isEqualTo("0");
  }

  @Test
  void rechaza_datos_invalidos_sin_persistir_nada() throws SQLException {
    var sinNombre = new AltaClinicaRequest(" ", null, null, "UTC", "Ana", "ana@vetcat.dev");
    var zonaInvalida =
        new AltaClinicaRequest("Zona mala", null, null, "Marte/Olimpo", "Ana", "ana@vetcat.dev");

    assertThatThrownBy(() -> servicio.aprovisionar(sinNombre))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> servicio.aprovisionar(zonaInvalida))
        .isInstanceOf(IllegalArgumentException.class);

    assertThat(cuenta("SELECT 1 FROM catalogo.clinica WHERE nombre = 'Zona mala'")).isEqualTo("0");
  }
}
