package com.vetcat.plataforma.service;

import com.vetcat.plataforma.repository.MigracionTenantRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Aplica el historial de migraciones de tenant a un esquema de clínica (ADR-0010).
 *
 * <p>Toma un advisory lock de PostgreSQL por esquema: dos altas, o un alta y el runner, sobre el
 * mismo esquema se serializan, y quien llega segundo encuentra el estado {@code aplicada} y no hace
 * nada. Solo migra esquemas registrados en {@code catalogo.migracion_tenant}.
 */
@Service
public class MigradorTenant {

  private static final Logger LOG = LoggerFactory.getLogger(MigradorTenant.class);
  private static final Pattern ESQUEMA_DE_CLINICA = Pattern.compile("clinica_\\d{3,}");
  private static final int ESPACIO_DE_LOCKS = 1;

  private final DataSource dataSource;
  private final MigracionTenantRepository estados;
  private final String ubicacion;

  public MigradorTenant(
      DataSource dataSource,
      MigracionTenantRepository estados,
      @Value("${vetcat.migraciones.tenant:filesystem:database/migrations/tenant}")
          String ubicacion) {
    this.dataSource = dataSource;
    this.estados = estados;
    this.ubicacion = ubicacion;
  }

  /** Crea el esquema si falta y aplica el historial; no hace nada si ya está {@code aplicada}. */
  public void aplicar(String esquema) {
    if (!ESQUEMA_DE_CLINICA.matcher(esquema).matches()) {
      throw new IllegalArgumentException("Esquema de clínica inválido");
    }
    try (Connection candado = dataSource.getConnection()) {
      tomar(candado, esquema);
      try {
        migrarBajoCandado(esquema);
      } finally {
        soltar(candado, esquema);
      }
    } catch (SQLException e) {
      throw new IllegalStateException("No se pudo tomar el candado de migración", e);
    }
  }

  private void migrarBajoCandado(String esquema) {
    String estado =
        estados
            .estadoDe(esquema)
            .orElseThrow(
                () -> new IllegalStateException("El esquema no está registrado para migrar"));
    if ("aplicada".equals(estado)) {
      return;
    }
    try {
      var flyway =
          Flyway.configure()
              .dataSource(dataSource)
              // `ext` aloja btree_gist y es la única ruta compartida (Decisiones_tecnicas.md §5).
              .schemas(esquema, "ext")
              .defaultSchema(esquema)
              .locations(ubicacion)
              .load();
      flyway.migrate();
      MigrationVersion version =
          flyway.info().current() == null ? null : flyway.info().current().getVersion();
      estados.marcarAplicada(esquema, version == null ? null : version.getVersion());
    } catch (RuntimeException e) {
      LOG.warn("Falló la migración de un esquema de clínica", e);
      estados.marcarFallida(esquema);
      throw e;
    }
  }

  private static void tomar(Connection conexion, String esquema) throws SQLException {
    try (PreparedStatement s =
        conexion.prepareStatement("SELECT pg_advisory_lock(?, hashtext(?))")) {
      s.setInt(1, ESPACIO_DE_LOCKS);
      s.setString(2, esquema);
      s.execute();
    }
  }

  private static void soltar(Connection conexion, String esquema) {
    try (PreparedStatement s =
        conexion.prepareStatement("SELECT pg_advisory_unlock(?, hashtext(?))")) {
      s.setInt(1, ESPACIO_DE_LOCKS);
      s.setString(2, esquema);
      s.execute();
    } catch (SQLException e) {
      // Un candado que no se suelta no puede volver al pool: se descarta la conexión.
      LOG.warn("No se pudo soltar el candado de migración; se descarta la conexión", e);
      try {
        conexion.abort(Runnable::run);
      } catch (SQLException | RuntimeException ignorada) {
        LOG.debug("La conexión ya estaba cerrada", ignorada);
      }
    }
  }
}
