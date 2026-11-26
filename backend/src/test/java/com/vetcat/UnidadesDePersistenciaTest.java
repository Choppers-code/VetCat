package com.vetcat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.tenancy.TenantAusenteException;
import com.vetcat.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Arranca el contexto con las dos unidades de persistencia contra un PostgreSQL real. */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba,com.vetcat.citas.entity",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"
    })
class UnidadesDePersistenciaTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void preparaCatalogoYDosClinicas() throws SQLException {
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
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement()) {
      for (int clinica : new int[] {1, 2}) {
        s.execute(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria) VALUES ('C"
                + clinica
                + "', 'UTC')");
        String esquema = "clinica_00" + clinica;
        s.execute("CREATE SCHEMA " + esquema);
        s.execute("CREATE TABLE " + esquema + ".mascota (id bigint PRIMARY KEY, nombre text)");
        s.execute("INSERT INTO " + esquema + ".mascota VALUES (1, 'clinica" + clinica + "-a')");
      }
    }
  }

  @Autowired ClinicaRepository clinicas;
  @Autowired UsuarioClinicaRepository membresias;
  @Autowired DataSource dataSource;

  @Autowired
  @Qualifier("tenantTransactionManager")
  PlatformTransactionManager tenantTx;

  @Autowired
  @Qualifier("tenantEntityManagerFactory")
  EntityManagerFactory tenantEmf;

  @Autowired
  @Qualifier("catalogoEntityManagerFactory")
  EntityManagerFactory catalogoEmf;

  @PersistenceContext(unitName = "tenant")
  EntityManager tenantEm;

  private List<String> mascotasDeLaClinicaActiva() {
    return new TransactionTemplate(tenantTx)
        .execute(
            estado ->
                tenantEm
                    .createQuery(
                        "select m.nombre from MascotaPrueba m order by m.nombre", String.class)
                    .getResultList());
  }

  @Test
  void la_unidad_del_catalogo_lee_clinicas_y_pertenencia() throws SQLException {
    UUID usuario = UUID.randomUUID();
    long clinica = clinicas.findAll().get(0).getId();
    try (Connection c = dataSource.getConnection();
        Statement s = c.createStatement()) {
      s.execute(
          "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol) VALUES ('"
              + usuario
              + "', "
              + clinica
              + ", 'veterinario')");
      s.execute(
          "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol) VALUES ('"
              + usuario
              + "', "
              + clinica
              + ", 'clinica_admin')");
    }

    List<UsuarioClinica> roles =
        membresias.findByKeycloakIdAndClinicaIdAndEstado(usuario, clinica, "ACTIVO");

    assertThat(roles)
        .extracting(UsuarioClinica::getRol)
        .containsExactlyInAnyOrder("veterinario", "clinica_admin");
    assertThat(clinicas.findById(clinica).orElseThrow().getEsquema())
        .isEqualTo("clinica_%03d".formatted(clinica));
  }

  @Test
  void la_unidad_de_tenant_lee_solo_el_esquema_de_la_clinica_activa() {
    assertThat(TenantContext.ejecutar(1L, this::mascotasDeLaClinicaActiva))
        .containsExactly("clinica1-a");
    assertThat(TenantContext.ejecutar(2L, this::mascotasDeLaClinicaActiva))
        .containsExactly("clinica2-a");
  }

  @Test
  void la_unidad_de_tenant_falla_cerrada_sin_tenant() {
    assertThatThrownBy(this::mascotasDeLaClinicaActiva)
        .hasRootCauseInstanceOf(TenantAusenteException.class);
  }

  @Test
  void ninguna_unidad_usa_cache_de_segundo_nivel() {
    for (EntityManagerFactory emf : List.of(tenantEmf, catalogoEmf)) {
      assertThat(
              emf.unwrap(SessionFactoryImplementor.class)
                  .getSessionFactoryOptions()
                  .isSecondLevelCacheEnabled())
          .isFalse();
    }
  }
}
