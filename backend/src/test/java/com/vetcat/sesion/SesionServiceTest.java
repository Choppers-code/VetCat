package com.vetcat.sesion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.security.TokenTenant;
import com.vetcat.security.TokenTenantService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** La sesión contra el catálogo real: quién puede abrir qué clínica y con qué roles. */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"
    })
class SesionServiceTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static UUID conDosClinicas;
  private static Long clinicaActiva;
  private static Long clinicaSuspendida;

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void migraYPreparaClinicas() throws SQLException {
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

    conDosClinicas = UUID.randomUUID();
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement()) {
      clinicaActiva = insertaClinica(s, "Clínica Norte", "ACTIVA");
      clinicaSuspendida = insertaClinica(s, "Clínica Sur", "SUSPENDIDA");
      s.execute(
          "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol) VALUES ('"
              + conDosClinicas
              + "', "
              + clinicaActiva
              + ", 'veterinario')");
      s.execute(
          "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol) VALUES ('"
              + conDosClinicas
              + "', "
              + clinicaActiva
              + ", 'clinica_admin')");
      s.execute(
          "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol, estado) VALUES ('"
              + conDosClinicas
              + "', "
              + clinicaSuspendida
              + ", 'recepcionista', 'INACTIVO')");
    }
  }

  private static Long insertaClinica(Statement s, String nombre, String estado)
      throws SQLException {
    var rs =
        s.executeQuery(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria, estado) VALUES ('"
                + nombre
                + "', 'UTC', '"
                + estado
                + "') RETURNING id");
    rs.next();
    return rs.getLong(1);
  }

  @Autowired SesionService sesion;
  @Autowired TokenTenantService tokens;

  @Test
  void lista_solo_las_clinicas_con_membresia_activa() {
    List<ClinicaDelUsuario> clinicas = sesion.clinicas(conDosClinicas);

    assertThat(clinicas).hasSize(1);
    assertThat(clinicas.get(0).id()).isEqualTo(clinicaActiva);
    assertThat(clinicas.get(0).nombre()).isEqualTo("Clínica Norte");
    assertThat(clinicas.get(0).roles()).containsExactly("clinica_admin", "veterinario");
  }

  @Test
  void abre_sesion_con_los_roles_efectivos_de_esa_clinica() {
    SesionResponse respuesta = sesion.abrir(conDosClinicas, clinicaActiva);

    TokenTenant leido = tokens.validar(respuesta.token());
    assertThat(leido.sujeto()).isEqualTo(conDosClinicas.toString());
    assertThat(leido.clinicaId()).isEqualTo(clinicaActiva);
    assertThat(leido.roles()).containsExactly("clinica_admin", "veterinario");
    assertThat(respuesta.roles()).containsExactly("clinica_admin", "veterinario");
  }

  @Test
  void rechaza_una_clinica_sin_pertenencia_activa() {
    assertThatThrownBy(() -> sesion.abrir(conDosClinicas, clinicaSuspendida))
        .isInstanceOf(SinAccesoALaClinicaException.class);
  }

  @Test
  void rechaza_una_clinica_inexistente() {
    assertThatThrownBy(() -> sesion.abrir(conDosClinicas, 999_999L))
        .isInstanceOf(SinAccesoALaClinicaException.class);
  }
}
