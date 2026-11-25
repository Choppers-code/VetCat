package com.vetcat.citas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.citas.entity.BloqueoAgenda;
import com.vetcat.citas.entity.Cita;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.entity.HorarioConsultorio;
import com.vetcat.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

@Testcontainers
@SpringBootTest(properties = {"vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"})
class EsquemaAgendaTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static String esquema;

  @PersistenceContext(unitName = "tenant")
  EntityManager tenantEm;

  @Autowired
  @Qualifier("tenantTransactionManager")
  PlatformTransactionManager tenantTx;

  @DynamicPropertySource
  static void registraConexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void migraCatalogoYTenant() throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement()) {
      sentencia.execute("CREATE SCHEMA catalogo");
    }

    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();

    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado =
            sentencia.executeQuery(
                "INSERT INTO catalogo.clinica (nombre, zona_horaria) "
                    + "VALUES ('Agenda', 'UTC') RETURNING esquema")) {
      resultado.next();
      esquema = resultado.getString(1);
    }

    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas(esquema, "ext")
        .defaultSchema(esquema)
        .locations("filesystem:../database/migrations/tenant")
        .load()
        .migrate();
  }

  @BeforeEach
  void limpiaDatos() throws SQLException {
    ejecuta(
        "TRUNCATE "
            + esquema
            + ".horario_consultorio, "
            + esquema
            + ".bloqueo_agenda, "
            + esquema
            + ".cita, "
            + esquema
            + ".consultorio, "
            + esquema
            + ".mascota, "
            + esquema
            + ".propietario RESTART IDENTITY CASCADE");
  }

  @Test
  void el_consultorio_rechaza_empalmes_y_las_canceladas_liberan_la_franja() throws SQLException {
    registraMascota();
    long consultorio = creaConsultorio("Consultorio 1");

    insertaCita(1, 101, consultorio, "PENDIENTE", "10:00", "11:00");

    assertThatThrownBy(() -> insertaCita(1, 102, consultorio, "PENDIENTE", "10:30", "11:30"))
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("cita_sin_empalmes_consultorio");

    insertaCita(
        1, 103, consultorio, "CANCELADA", "12:00", "13:00", 103, "Cancelada por la clínica");
    insertaCita(1, 103, consultorio, "PENDIENTE", "12:00", "13:00");

    assertThat(texto("SELECT count(*) FROM " + esquema + ".cita")).isEqualTo("3");
  }

  @Test
  void el_veterinario_no_puede_tener_empalmes_en_consultorios_distintos() throws SQLException {
    registraMascota();
    long consultorioUno = creaConsultorio("Consultorio 1");
    long consultorioDos = creaConsultorio("Consultorio 2");
    insertaCita(1, 201, consultorioUno, "PENDIENTE", "10:00", "11:00");

    assertThatThrownBy(() -> insertaCita(1, 201, consultorioDos, "PENDIENTE", "10:30", "11:30"))
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("cita_sin_empalmes_veterinario");
  }

  @Test
  void las_entidades_jpa_se_persisten_en_el_esquema_tenant() throws SQLException {
    registraMascota();

    TenantContext.ejecutar(
        1L,
        () ->
            new TransactionTemplate(tenantTx)
                .execute(
                    estado -> {
                      var consultorio = new Consultorio("Consultorio JPA");
                      tenantEm.persist(consultorio);
                      tenantEm.flush();

                      tenantEm.persist(
                          new HorarioConsultorio(
                              consultorio.getId(),
                              (short) 1,
                              null,
                              LocalTime.of(9, 0),
                              LocalTime.of(17, 0),
                              false));
                      tenantEm.persist(
                          new HorarioConsultorio(
                              consultorio.getId(),
                              null,
                              LocalDate.of(2026, 12, 25),
                              null,
                              null,
                              true));
                      tenantEm.persist(
                          new BloqueoAgenda(
                              301L,
                              null,
                              Instant.parse("2026-12-01T10:00:00Z"),
                              Instant.parse("2026-12-01T11:00:00Z"),
                              "Capacitación"));
                      tenantEm.persist(
                          new Cita(
                              1L,
                              301L,
                              consultorio.getId(),
                              "CONSULTORIO",
                              null,
                              Instant.parse("2026-12-01T11:00:00Z"),
                              Instant.parse("2026-12-01T12:00:00Z"),
                              "Consulta",
                              "PERSONAL"));
                      tenantEm.flush();
                      return null;
                    }));

    assertThat(texto("SELECT count(*) FROM " + esquema + ".horario_consultorio")).isEqualTo("2");
    assertThat(texto("SELECT count(*) FROM " + esquema + ".bloqueo_agenda")).isEqualTo("1");
    assertThat(texto("SELECT mascota_id FROM " + esquema + ".cita")).isEqualTo("1");
  }

  private static Connection conexion() throws SQLException {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  }

  private static void ejecuta(String sql) throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement()) {
      sentencia.execute(sql);
    }
  }

  private static String texto(String sql) throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado = sentencia.executeQuery(sql)) {
      resultado.next();
      return resultado.getString(1);
    }
  }

  private static long creaConsultorio(String nombre) throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado =
            sentencia.executeQuery(
                "INSERT INTO "
                    + esquema
                    + ".consultorio (nombre) VALUES ('"
                    + nombre
                    + "') RETURNING id")) {
      resultado.next();
      return resultado.getLong(1);
    }
  }

  private static void registraMascota() throws SQLException {
    ejecuta("INSERT INTO " + esquema + ".propietario (nombre, telefono) VALUES ('Ana', '555')");
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".mascota (propietario_id, nombre, especie) VALUES (1, 'Luna', 'GATO')");
  }

  private static void insertaCita(
      long mascotaId,
      long veterinarioId,
      long consultorioId,
      String estado,
      String inicio,
      String fin)
      throws SQLException {
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".cita (mascota_id, veterinario_id, consultorio_id, tipo, inicio, fin, estado) "
            + "VALUES ("
            + mascotaId
            + ", "
            + veterinarioId
            + ", "
            + consultorioId
            + ", 'CONSULTORIO', '2026-12-01 "
            + inicio
            + ":00+00', '2026-12-01 "
            + fin
            + ":00+00', '"
            + estado
            + "')");
  }

  private static void insertaCita(
      long mascotaId,
      long veterinarioId,
      long consultorioId,
      String estado,
      String inicio,
      String fin,
      long canceladaPor,
      String motivoCancelacion)
      throws SQLException {
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".cita (mascota_id, veterinario_id, consultorio_id, tipo, inicio, fin, estado, "
            + "cancelada_por, motivo_cancelacion) VALUES ("
            + mascotaId
            + ", "
            + veterinarioId
            + ", "
            + consultorioId
            + ", 'CONSULTORIO', '2026-12-01 "
            + inicio
            + ":00+00', '2026-12-01 "
            + fin
            + ":00+00', '"
            + estado
            + "', "
            + canceladaPor
            + ", '"
            + motivoCancelacion
            + "')");
  }
}
