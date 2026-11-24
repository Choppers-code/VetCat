package com.vetcat.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Dos clínicas, un pool compartido y ningún cruce de datos (ADR-0011). */
@Testcontainers
class AislamientoDeTenantTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static HikariDataSource pool;

  @BeforeAll
  static void preparaDosClinicas() throws SQLException {
    pool = nuevoPool(2);
    try (Connection c = pool.getConnection();
        Statement s = c.createStatement()) {
      s.execute("CREATE SCHEMA ext");
      for (int clinica : new int[] {1, 2}) {
        String esquema = "clinica_00" + clinica;
        s.execute("CREATE SCHEMA " + esquema);
        s.execute("CREATE TABLE " + esquema + ".mascota (nombre text NOT NULL)");
        s.execute("INSERT INTO " + esquema + ".mascota VALUES ('clinica" + clinica + "-a')");
        s.execute("INSERT INTO " + esquema + ".mascota VALUES ('clinica" + clinica + "-b')");
      }
    }
  }

  @AfterAll
  static void cierraElPool() {
    pool.close();
  }

  private static HikariDataSource nuevoPool(int tamano) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(POSTGRES.getJdbcUrl());
    config.setUsername(POSTGRES.getUsername());
    config.setPassword(POSTGRES.getPassword());
    config.setMaximumPoolSize(tamano);
    config.setMinimumIdle(tamano);
    config.setConnectionTimeout(10_000);
    return new HikariDataSource(config);
  }

  private static List<String> mascotas(Connection c) throws SQLException {
    List<String> nombres = new ArrayList<>();
    try (Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT nombre FROM mascota ORDER BY nombre")) {
      while (rs.next()) {
        nombres.add(rs.getString(1));
      }
    }
    return nombres;
  }

  private static String texto(Connection c, String sql) throws SQLException {
    try (Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(sql)) {
      rs.next();
      return rs.getString(1);
    }
  }

  @Test
  void cada_clinica_solo_ve_sus_propias_filas() throws SQLException {
    var provider = new SchemaMultiTenantConnectionProvider(pool);

    Connection uno = provider.getConnection(1L);
    try {
      assertThat(mascotas(uno)).containsExactly("clinica1-a", "clinica1-b");
    } finally {
      provider.releaseConnection(1L, uno);
    }
    Connection dos = provider.getConnection(2L);
    try {
      assertThat(mascotas(dos)).containsExactly("clinica2-a", "clinica2-b");
    } finally {
      provider.releaseConnection(2L, dos);
    }
  }

  @Test
  void una_conexion_sin_tenant_no_puede_leer_tablas_de_negocio() throws SQLException {
    var provider = new SchemaMultiTenantConnectionProvider(pool);

    Connection sinTenant = provider.getAnyConnection();
    try {
      assertThatThrownBy(() -> mascotas(sinTenant)).isInstanceOf(SQLException.class);
    } finally {
      provider.releaseAnyConnection(sinTenant);
    }
  }

  @Test
  void la_conexion_devuelta_no_conserva_el_search_path_de_la_clinica() throws SQLException {
    try (HikariDataSource unico = nuevoPool(1)) {
      var provider = new SchemaMultiTenantConnectionProvider(unico);
      Connection conexion = provider.getConnection(1L);
      provider.releaseConnection(1L, conexion);

      // Con un pool de una sola conexión, esta es la misma conexión física.
      Connection reutilizada = provider.getAnyConnection();
      try {
        assertThat(texto(reutilizada, "SHOW search_path")).isEqualTo("\"\"");
      } finally {
        provider.releaseAnyConnection(reutilizada);
      }
    }
  }

  @Test
  void descarta_la_conexion_si_no_se_puede_restablecer_el_search_path() throws SQLException {
    try (HikariDataSource unico = nuevoPool(1)) {
      var provider = new SchemaMultiTenantConnectionProvider(unico);
      Connection conexion = provider.getConnection(1L);
      String pidAntes = texto(conexion, "SELECT pg_backend_pid()");

      // Una transacción abortada hace fallar el SET del restablecimiento.
      conexion.setAutoCommit(false);
      try (Statement s = conexion.createStatement()) {
        assertThatThrownBy(() -> s.execute("SELECT 1/0")).isInstanceOf(SQLException.class);
      }
      provider.releaseConnection(1L, conexion);

      Connection nueva = provider.getAnyConnection();
      try {
        assertThat(texto(nueva, "SELECT pg_backend_pid()")).isNotEqualTo(pidAntes);
        assertThat(texto(nueva, "SHOW search_path")).isEqualTo("\"\"");
      } finally {
        provider.releaseAnyConnection(nueva);
      }
    }
  }

  @Test
  void rechaza_un_tenant_ausente_o_invalido() {
    var provider = new SchemaMultiTenantConnectionProvider(pool);

    assertThatThrownBy(() -> provider.getConnection(null))
        .isInstanceOf(TenantAusenteException.class);
    assertThatThrownBy(() -> provider.getConnection(0L)).isInstanceOf(TenantAusenteException.class);
    assertThatThrownBy(() -> new CurrentTenantResolver().resolveCurrentTenantIdentifier())
        .isInstanceOf(TenantAusenteException.class);
  }

  @Test
  void el_contexto_restablece_el_tenant_anterior_al_terminar() {
    assertThat(TenantContext.actual()).isEmpty();

    Long dentro =
        TenantContext.ejecutar(
            7L,
            () -> {
              Long interno = TenantContext.ejecutar(9L, () -> TenantContext.actual().orElseThrow());
              assertThat(interno).isEqualTo(9L);
              return TenantContext.actual().orElseThrow();
            });

    assertThat(dentro).isEqualTo(7L);
    assertThat(TenantContext.actual()).isEmpty();
  }

  @Test
  void bajo_concurrencia_ninguna_peticion_lee_filas_de_otra_clinica() throws Exception {
    var provider = new SchemaMultiTenantConnectionProvider(pool);
    int hilos = 8;
    int vueltas = 150;
    AtomicInteger cruces = new AtomicInteger();
    AtomicInteger errores = new AtomicInteger();
    CountDownLatch salida = new CountDownLatch(1);
    ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);

    List<Future<?>> tareas = new ArrayList<>();
    for (int h = 0; h < hilos; h++) {
      final int hilo = h;
      tareas.add(
          ejecutor.submit(
              () -> {
                salida.await();
                for (int i = 0; i < vueltas; i++) {
                  long clinica = ((i + hilo) % 2) + 1;
                  try {
                    if (i % 5 == 0) {
                      // Una petición sin tenant no debe heredar el esquema de otra.
                      Connection c = provider.getAnyConnection();
                      try {
                        mascotas(c);
                        cruces.incrementAndGet();
                      } catch (SQLException esperado) {
                        // correcto: sin tenant no hay tabla
                      } finally {
                        provider.releaseAnyConnection(c);
                      }
                    } else {
                      Connection c = provider.getConnection(clinica);
                      try {
                        for (String nombre : mascotas(c)) {
                          if (!nombre.startsWith("clinica" + clinica + "-")) {
                            cruces.incrementAndGet();
                          }
                        }
                      } finally {
                        provider.releaseConnection(clinica, c);
                      }
                    }
                  } catch (SQLException e) {
                    errores.incrementAndGet();
                  }
                }
                return null;
              }));
    }
    salida.countDown();
    for (Future<?> tarea : tareas) {
      tarea.get(60, TimeUnit.SECONDS);
    }
    ejecutor.shutdown();

    assertThat(cruces.get()).as("lecturas de otra clínica").isZero();
    assertThat(errores.get()).as("errores inesperados").isZero();
  }
}
