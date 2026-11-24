package com.vetcat.tenancy;

import java.io.Serial;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrega conexiones con el {@code search_path} de la clínica y lo restablece al devolverlas
 * (ADR-0011).
 *
 * <p>El estado vive en la sesión de PostgreSQL, no en el hilo: limpiar el {@link TenantContext} no
 * limpia la conexión física. Por eso se restablece en un {@code finally} y, si no se puede, la
 * conexión se descarta del pool en vez de reutilizarse.
 */
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<Long> {

  @Serial private static final long serialVersionUID = 1L;

  private static final Logger LOG =
      LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);

  private final transient DataSource dataSource;

  public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** Esquema de la clínica, derivado del identificador numérico: nunca de texto del cliente. */
  static String esquemaDe(Long tenantId) {
    if (tenantId == null || tenantId <= 0) {
      throw new TenantAusenteException();
    }
    return "clinica_%03d".formatted(tenantId);
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    Connection connection = dataSource.getConnection();
    try {
      // Una conexión nueva trae el search_path por defecto de PostgreSQL; sin tenant debe ser
      // vacío.
      vaciarSearchPath(connection);
      return connection;
    } catch (SQLException | RuntimeException e) {
      descartar(connection);
      throw e;
    }
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    devolver(connection);
  }

  @Override
  public Connection getConnection(Long tenantId) throws SQLException {
    String esquema = esquemaDe(tenantId);
    Connection connection = dataSource.getConnection();
    try (PreparedStatement fija =
        connection.prepareStatement("SELECT set_config('search_path', ?, false)")) {
      // `ext` aloja btree_gist y es la única ruta compartida (Decisiones_tecnicas.md §5).
      fija.setString(1, esquema + ", ext");
      fija.execute();
      return connection;
    } catch (SQLException | RuntimeException e) {
      descartar(connection);
      throw e;
    }
  }

  @Override
  public void releaseConnection(Long tenantId, Connection connection) throws SQLException {
    devolver(connection);
  }

  /** Restablece el {@code search_path} y devuelve la conexión; si falla, la descarta. */
  private void devolver(Connection connection) {
    try {
      vaciarSearchPath(connection);
    } catch (SQLException | RuntimeException e) {
      LOG.warn("No se pudo restablecer el search_path; se descarta la conexión", e);
      descartar(connection);
      return;
    }
    cerrar(connection);
  }

  private static void vaciarSearchPath(Connection connection) throws SQLException {
    try (Statement s = connection.createStatement()) {
      s.execute("SET search_path TO ''");
    }
  }

  private static void descartar(Connection connection) {
    try {
      connection.abort(Runnable::run);
    } catch (SQLException | RuntimeException e) {
      LOG.debug("La conexión ya estaba cerrada", e);
    }
    cerrar(connection);
  }

  private static void cerrar(Connection connection) {
    try {
      connection.close();
    } catch (SQLException | RuntimeException e) {
      LOG.debug("No se pudo cerrar la conexión", e);
    }
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return unwrapType.isInstance(this) || MultiTenantConnectionProvider.class.equals(unwrapType);
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    if (isUnwrappableAs(unwrapType)) {
      return unwrapType.cast(this);
    }
    throw new IllegalArgumentException("No se puede convertir a " + unwrapType);
  }
}
