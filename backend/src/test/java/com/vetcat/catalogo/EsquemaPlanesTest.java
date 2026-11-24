package com.vetcat.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** El modelo de planes contra PostgreSQL real: funciones, límites y pagos. */
@Testcontainers
class EsquemaPlanesTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  @BeforeAll
  static void aplicaMigracionesDelCatalogo() throws SQLException {
    try (Connection c = conexion();
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

  private static Connection conexion() throws SQLException {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  }

  private static void ejecuta(String sql) throws SQLException {
    try (Connection c = conexion();
        Statement s = c.createStatement()) {
      s.execute(sql);
    }
  }

  private static String consultaTexto(String sql) throws SQLException {
    try (Connection c = conexion();
        Statement s = c.createStatement();
        var rs = s.executeQuery(sql)) {
      rs.next();
      return rs.getString(1);
    }
  }

  private static long creaPlan(String nombre) throws SQLException {
    return Long.parseLong(
        consultaTexto("INSERT INTO catalogo.plan (nombre) VALUES ('" + nombre + "') RETURNING id"));
  }

  private static long creaClinicaConPlan(String nombre, String esquemaIgnorado)
      throws SQLException {
    return Long.parseLong(
        consultaTexto(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria) VALUES ('"
                + nombre
                + "', 'UTC') RETURNING id"));
  }

  @Test
  void guarda_un_plan_con_funciones_y_limites() throws SQLException {
    long plan = creaPlan("Básico");
    ejecuta(
        "INSERT INTO catalogo.plan_funcion (plan_id, funcion) VALUES ("
            + plan
            + ", 'portal_cliente')");
    ejecuta("INSERT INTO catalogo.plan_funcion (plan_id, funcion) VALUES (" + plan + ", 'ventas')");
    ejecuta(
        "INSERT INTO catalogo.plan_limite (plan_id, recurso, maximo) VALUES ("
            + plan
            + ", 'mascotas', 200)");

    assertThat(consultaTexto("SELECT count(*) FROM catalogo.plan_funcion WHERE plan_id = " + plan))
        .isEqualTo("2");
    assertThat(consultaTexto("SELECT maximo FROM catalogo.plan_limite WHERE plan_id = " + plan))
        .isEqualTo("200");
  }

  @Test
  void rechaza_dos_planes_con_el_mismo_nombre() throws SQLException {
    creaPlan("Repetido");

    assertThatThrownBy(() -> creaPlan("Repetido")).isInstanceOf(SQLException.class);
  }

  @Test
  void rechaza_una_funcion_de_plan_desconocida() throws SQLException {
    long plan = creaPlan("Función rara");

    assertThatThrownBy(
            () ->
                ejecuta(
                    "INSERT INTO catalogo.plan_funcion (plan_id, funcion) VALUES ("
                        + plan
                        + ", 'telepatia')"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void rechaza_un_limite_no_positivo() throws SQLException {
    long plan = creaPlan("Límite cero");

    assertThatThrownBy(
            () ->
                ejecuta(
                    "INSERT INTO catalogo.plan_limite (plan_id, recurso, maximo) VALUES ("
                        + plan
                        + ", 'usuarios', 0)"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void no_deja_borrar_un_plan_que_usa_una_clinica() throws SQLException {
    long plan = creaPlan("En uso");
    long clinica = creaClinicaConPlan("Clínica con plan", "clinica_php");
    ejecuta("UPDATE catalogo.clinica SET plan_id = " + plan + " WHERE id = " + clinica);

    assertThatThrownBy(() -> ejecuta("DELETE FROM catalogo.plan WHERE id = " + plan))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void rechaza_un_pago_con_el_periodo_invertido() throws SQLException {
    long clinica = creaClinicaConPlan("Clínica pago", "clinica_pag");

    assertThatThrownBy(
            () ->
                ejecuta(
                    "INSERT INTO catalogo.pago_plan (clinica_id, monto, periodo_desde, periodo_hasta, autor)"
                        + " VALUES ("
                        + clinica
                        + ", 100.00, '2026-12-01', '2026-11-01', 'super_admin')"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void la_clinica_sin_plan_sigue_siendo_valida() throws SQLException {
    long clinica = creaClinicaConPlan("Sin plan", "clinica_sin");

    assertThat(
            consultaTexto(
                "SELECT coalesce(plan_id::text, 'nulo') FROM catalogo.clinica WHERE id = "
                    + clinica))
        .isEqualTo("nulo");
  }
}
