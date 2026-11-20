package com.vetcat.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Valida las migraciones del catálogo contra un PostgreSQL real. */
@Testcontainers
class EsquemaCatalogoTest {

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

  private static long creaClinica(String nombre) throws SQLException {
    return Long.parseLong(
        consultaTexto(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria) VALUES ('"
                + nombre
                + "', 'America/Mexico_City') RETURNING id"));
  }

  @Test
  void genera_el_esquema_de_la_clinica_a_partir_del_id() throws SQLException {
    long id = creaClinica("Clínica Uno");

    assertThat(consultaTexto("SELECT esquema FROM catalogo.clinica WHERE id = " + id))
        .isEqualTo("clinica_" + String.format("%03d", id));
  }

  @Test
  void rechaza_que_el_cliente_fije_el_esquema() {
    assertThatThrownBy(
            () ->
                ejecuta(
                    "INSERT INTO catalogo.clinica (nombre, zona_horaria, esquema)"
                        + " VALUES ('X', 'UTC', 'clinica_999')"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void acepta_dos_clinicas_con_el_mismo_nombre() throws SQLException {
    long a = creaClinica("Repetida");
    long b = creaClinica("Repetida");

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void rechaza_un_estado_de_clinica_invalido() throws SQLException {
    long id = creaClinica("Estado");

    assertThatThrownBy(
            () -> ejecuta("UPDATE catalogo.clinica SET estado = 'ELIMINADA' WHERE id = " + id))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void permite_varios_roles_del_mismo_usuario_en_una_clinica() throws SQLException {
    long clinica = creaClinica("Roles");
    String usuario = UUID.randomUUID().toString();

    ejecuta(insertaMembresia(usuario, clinica, "veterinario"));
    ejecuta(insertaMembresia(usuario, clinica, "clinica_admin"));

    assertThat(
            consultaTexto(
                "SELECT count(*) FROM catalogo.usuario_clinica WHERE keycloak_id = '"
                    + usuario
                    + "'"))
        .isEqualTo("2");
  }

  @Test
  void rechaza_el_mismo_rol_repetido_en_la_misma_clinica() throws SQLException {
    long clinica = creaClinica("Duplicado");
    String usuario = UUID.randomUUID().toString();
    ejecuta(insertaMembresia(usuario, clinica, "recepcionista"));

    assertThatThrownBy(() -> ejecuta(insertaMembresia(usuario, clinica, "recepcionista")))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void permite_que_un_usuario_pertenezca_a_varias_clinicas() throws SQLException {
    String usuario = UUID.randomUUID().toString();

    ejecuta(insertaMembresia(usuario, creaClinica("A"), "veterinario"));
    ejecuta(insertaMembresia(usuario, creaClinica("B"), "recepcionista"));

    assertThat(
            consultaTexto(
                "SELECT count(DISTINCT clinica_id) FROM catalogo.usuario_clinica"
                    + " WHERE keycloak_id = '"
                    + usuario
                    + "'"))
        .isEqualTo("2");
  }

  @Test
  void rechaza_super_admin_en_usuario_clinica() throws SQLException {
    long clinica = creaClinica("SuperAdmin");

    assertThatThrownBy(
            () -> ejecuta(insertaMembresia(UUID.randomUUID().toString(), clinica, "super_admin")))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void rechaza_una_membresia_de_una_clinica_inexistente() {
    assertThatThrownBy(
            () -> ejecuta(insertaMembresia(UUID.randomUUID().toString(), 999_999L, "cliente")))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void rechaza_estado_de_migracion_de_un_esquema_sin_clinica() {
    assertThatThrownBy(
            () -> ejecuta("INSERT INTO catalogo.migracion_tenant (esquema) VALUES ('clinica_998')"))
        .isInstanceOf(SQLException.class);
  }

  private static String insertaMembresia(String usuario, long clinica, String rol) {
    return "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol) VALUES ('"
        + usuario
        + "', "
        + clinica
        + ", '"
        + rol
        + "')";
  }
}
