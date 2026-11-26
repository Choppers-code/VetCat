package com.vetcat.plataforma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.plataforma.entity.AccionDePlataforma;
import com.vetcat.plataforma.service.AuditoriaService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

/** La auditoría de plataforma: lo que se registra, lo que no, y cómo se lee. */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba,com.vetcat.citas.entity",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"
    })
class AuditoriaTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static Long clinica;

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void migraYPreparaUnaClinica() throws SQLException {
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
        Statement s = c.createStatement();
        var rs =
            s.executeQuery(
                "INSERT INTO catalogo.clinica (nombre, zona_horaria) VALUES ('Auditada', 'UTC')"
                    + " RETURNING id")) {
      rs.next();
      clinica = rs.getLong(1);
    }
  }

  private static void ejecuta(String sql) throws SQLException {
    try (Connection c =
            DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        Statement s = c.createStatement()) {
      s.execute(sql);
    }
  }

  @Autowired AuditoriaService auditoria;

  @Test
  void registra_quien_hizo_que_y_sobre_que_clinica() {
    UUID actor = UUID.randomUUID();

    auditoria.registrar(actor, AccionDePlataforma.CAMBIO_PLAN, clinica, "Básico -> Completo");

    var filas = auditoria.ultimasDeClinica(clinica);
    assertThat(filas).isNotEmpty();
    assertThat(filas.get(0).actorKeycloakId()).isEqualTo(actor);
    assertThat(filas.get(0).accion()).isEqualTo(AccionDePlataforma.CAMBIO_PLAN);
    assertThat(filas.get(0).detalle()).isEqualTo("Básico -> Completo");
  }

  @Test
  void registra_una_accion_de_catalogo_sin_clinica() {
    auditoria.registrar(UUID.randomUUID(), AccionDePlataforma.ALTA_PLAN, null, "Plan Completo");

    assertThat(auditoria.ultimasDeClinica(null)).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void rechaza_una_accion_que_no_esta_en_el_catalogo() {
    assertThatThrownBy(
            () ->
                ejecuta(
                    "INSERT INTO catalogo.auditoria_plataforma (actor_keycloak_id, accion)"
                        + " VALUES ('"
                        + UUID.randomUUID()
                        + "', 'BORRAR_TODO')"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void las_ultimas_de_una_clinica_vienen_de_la_mas_reciente_a_la_mas_antigua() {
    auditoria.registrar(UUID.randomUUID(), AccionDePlataforma.ALTA_CLINICA, clinica, "primera");
    auditoria.registrar(UUID.randomUUID(), AccionDePlataforma.PAGO_REGISTRADO, clinica, "segunda");

    var filas = auditoria.ultimasDeClinica(clinica);
    assertThat(filas.get(0).detalle()).isEqualTo("segunda");
    assertThat(filas.get(1).detalle()).isEqualTo("primera");
  }
}
