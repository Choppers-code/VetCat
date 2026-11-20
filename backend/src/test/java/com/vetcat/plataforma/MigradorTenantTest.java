package com.vetcat.plataforma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.plataforma.repository.MigracionTenantRepository;
import com.vetcat.plataforma.service.MigradorTenant;
import com.vetcat.plataforma.service.ReconciliacionTenantService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Migración de esquemas de clínica con advisory lock, estados y reconciliación (ADR-0010). */
@Testcontainers
class MigradorTenantTest {

  private static final String BUENAS = "filesystem:src/test/resources/migraciones-tenant";
  private static final String ROTAS = "filesystem:src/test/resources/migraciones-tenant-rota";

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static HikariDataSource pool;
  private static MigracionTenantRepository estados;

  @BeforeAll
  static void preparaElCatalogo() throws SQLException {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(POSTGRES.getJdbcUrl());
    config.setUsername(POSTGRES.getUsername());
    config.setPassword(POSTGRES.getPassword());
    config.setMaximumPoolSize(12);
    pool = new HikariDataSource(config);
    ejecuta("CREATE SCHEMA catalogo");
    Flyway.configure()
        .dataSource(pool)
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();
    estados = new MigracionTenantRepository(pool);
  }

  @AfterAll
  static void cierraElPool() {
    pool.close();
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

  /** Registra una clínica nueva y su esquema como pendiente; devuelve el nombre del esquema. */
  private static String clinicaPendiente() throws SQLException {
    String esquema =
        texto(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria) VALUES ('X', 'UTC') RETURNING esquema");
    estados.registrarPendiente(esquema);
    return esquema;
  }

  private static MigradorTenant migrador(String ubicacion) {
    return new MigradorTenant(pool, estados, ubicacion);
  }

  @Test
  void aplica_el_historial_al_esquema_nuevo_y_lo_marca_aplicado() throws SQLException {
    String esquema = clinicaPendiente();

    migrador(BUENAS).aplicar(esquema);

    assertThat(texto("SELECT to_regclass('" + esquema + ".mascota')::text"))
        .isEqualTo(esquema + ".mascota");
    assertThat(estados.estadoDe(esquema)).contains("aplicada");
    assertThat(
            texto(
                "SELECT version_aplicada FROM catalogo.migracion_tenant WHERE esquema = '"
                    + esquema
                    + "'"))
        .isEqualTo("1");
  }

  @Test
  void es_idempotente_si_ya_esta_aplicada() throws SQLException {
    String esquema = clinicaPendiente();
    var migrador = migrador(BUENAS);

    migrador.aplicar(esquema);
    migrador.aplicar(esquema);

    assertThat(texto("SELECT count(*) FROM " + esquema + ".flyway_schema_history")).isEqualTo("2");
  }

  @Test
  void rechaza_un_esquema_con_formato_invalido_o_sin_registrar() {
    assertThatThrownBy(() -> migrador(BUENAS).aplicar("public; DROP SCHEMA catalogo"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> migrador(BUENAS).aplicar("clinica_999"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void dos_migraciones_simultaneas_del_mismo_esquema_se_aplican_una_sola_vez() throws Exception {
    String esquema = clinicaPendiente();
    int hilos = 6;
    CountDownLatch salida = new CountDownLatch(1);
    ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
    List<Future<?>> tareas = new ArrayList<>();
    for (int i = 0; i < hilos; i++) {
      tareas.add(
          ejecutor.submit(
              () -> {
                salida.await();
                migrador(BUENAS).aplicar(esquema);
                return null;
              }));
    }
    salida.countDown();
    for (Future<?> tarea : tareas) {
      tarea.get(60, TimeUnit.SECONDS);
    }
    ejecutor.shutdown();

    // La historia de Flyway guarda la creación del esquema y la V1: una sola vez cada una.
    assertThat(texto("SELECT count(*) FROM " + esquema + ".flyway_schema_history")).isEqualTo("2");
    assertThat(estados.estadoDe(esquema)).contains("aplicada");
  }

  @Test
  void una_migracion_rota_deja_el_esquema_fallido() throws SQLException {
    String esquema = clinicaPendiente();

    assertThatThrownBy(() -> migrador(ROTAS).aplicar(esquema)).isInstanceOf(RuntimeException.class);

    assertThat(estados.estadoDe(esquema)).contains("fallida");
  }

  @Test
  void la_reconciliacion_reintenta_los_fallidos_y_los_deja_aplicados() throws SQLException {
    String esquema = clinicaPendiente();
    assertThatThrownBy(() -> migrador(ROTAS).aplicar(esquema)).isInstanceOf(RuntimeException.class);
    assertThat(estados.estadoDe(esquema)).contains("fallida");

    int reparados =
        new ReconciliacionTenantService(estados, migrador(BUENAS)).reconciliar(Duration.ZERO);

    assertThat(reparados).isGreaterThanOrEqualTo(1);
    assertThat(estados.estadoDe(esquema)).contains("aplicada");
  }

  @Test
  void la_reconciliacion_no_toca_un_alta_reciente() throws SQLException {
    String esquema = clinicaPendiente();

    int reparados =
        new ReconciliacionTenantService(estados, migrador(BUENAS)).reconciliar(Duration.ofHours(1));

    assertThat(reparados).isZero();
    assertThat(estados.estadoDe(esquema)).contains("pendiente");
  }
}
