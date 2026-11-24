package com.vetcat.propietarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.plataforma.repository.MigracionTenantRepository;
import com.vetcat.plataforma.service.MigradorTenant;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Esquema real de propietarios y mascotas (tenant/V1) y la extensión unaccent (catálogo/V6). */
@Testcontainers
class EsquemaPropietariosTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static HikariDataSource pool;
  private static String esquema;

  @BeforeAll
  static void migraElCatalogoYUnaClinica() throws SQLException {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(POSTGRES.getJdbcUrl());
    config.setUsername(POSTGRES.getUsername());
    config.setPassword(POSTGRES.getPassword());
    pool = new HikariDataSource(config);
    ejecuta("CREATE SCHEMA catalogo");
    Flyway.configure()
        .dataSource(pool)
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();
    var estados = new MigracionTenantRepository(pool);
    esquema =
        texto(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria) VALUES ('X', 'UTC') RETURNING esquema");
    estados.registrarPendiente(esquema);
    new MigradorTenant(pool, estados, "filesystem:../database/migrations/tenant").aplicar(esquema);
  }

  @AfterAll
  static void cierraElPool() {
    pool.close();
  }

  @BeforeEach
  void limpia() throws SQLException {
    ejecuta(
        "TRUNCATE " + esquema + ".mascota, " + esquema + ".propietario RESTART IDENTITY CASCADE");
  }

  private static void ejecuta(String sql) throws SQLException {
    try (Connection c = pool.getConnection();
        Statement s = c.createStatement()) {
      s.execute(sql);
    }
  }

  private static String texto(String sql) throws SQLException {
    try (Connection c = pool.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(sql)) {
      rs.next();
      return rs.getString(1);
    }
  }

  private static void propietario() throws SQLException {
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".propietario (nombre, telefono) VALUES ('Ana', '(55) 1234-5678')");
  }

  private static String mascota(String columnas, String valores) {
    return "INSERT INTO "
        + esquema
        + ".mascota (propietario_id, nombre, "
        + columnas
        + ") VALUES (1, 'Luna', "
        + valores
        + ")";
  }

  @Test
  void normaliza_el_telefono_dejando_solo_digitos() throws SQLException {
    propietario();

    assertThat(texto("SELECT telefono_normalizado FROM " + esquema + ".propietario"))
        .isEqualTo("5512345678");
  }

  @Test
  void acepta_una_mascota_valida() throws SQLException {
    propietario();

    ejecuta(
        mascota(
            "especie, sexo, peso_kg, fecha_nacimiento", "'GATO', 'HEMBRA', 4.25, '2020-01-01'"));

    assertThat(texto("SELECT count(*) FROM " + esquema + ".mascota")).isEqualTo("1");
  }

  @Test
  void rechaza_especie_sexo_peso_y_fecha_invalidos() throws SQLException {
    propietario();

    assertThatThrownBy(() -> ejecuta(mascota("especie", "'DRAGON'")))
        .hasMessageContaining("mascota_especie_valida");
    assertThatThrownBy(() -> ejecuta(mascota("especie, sexo", "'PERRO', 'X'")))
        .hasMessageContaining("mascota_sexo_valido");
    assertThatThrownBy(() -> ejecuta(mascota("especie, peso_kg", "'PERRO', 0")))
        .hasMessageContaining("mascota_peso_positivo");
    assertThatThrownBy(
            () -> ejecuta(mascota("especie, fecha_nacimiento", "'PERRO', CURRENT_DATE + 1")))
        .hasMessageContaining("mascota_nacimiento_no_futuro");
  }

  @Test
  void el_trigger_rechaza_cambiar_el_propietario_por_sql_directo() throws SQLException {
    propietario();
    propietario();
    ejecuta(mascota("especie", "'PERRO'"));

    assertThatThrownBy(() -> ejecuta("UPDATE " + esquema + ".mascota SET propietario_id = 2"))
        .hasMessageContaining("no puede cambiar de propietario");

    ejecuta("UPDATE " + esquema + ".mascota SET nombre = 'Otra' WHERE id = 1");
    assertThat(texto("SELECT propietario_id FROM " + esquema + ".mascota")).isEqualTo("1");
  }

  @Test
  void unaccent_ignora_acentos_desde_el_esquema_ext() throws SQLException {
    assertThat(texto("SELECT ext.unaccent('José Ñandú')")).isEqualTo("Jose Nandu");
  }
}
