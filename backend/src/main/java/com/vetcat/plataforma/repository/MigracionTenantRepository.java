package com.vetcat.plataforma.repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Estado de migración de cada esquema de clínica ({@code catalogo.migracion_tenant}).
 *
 * <p>Es infraestructura de la plataforma, no dato de ninguna clínica: vive en el catálogo.
 */
@Repository
public class MigracionTenantRepository {

  private final JdbcClient jdbc;

  public MigracionTenantRepository(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  /** Registra el esquema como pendiente; si ya figura, no lo toca. */
  public void registrarPendiente(String esquema) {
    jdbc.sql("INSERT INTO catalogo.migracion_tenant (esquema) VALUES (?) ON CONFLICT DO NOTHING")
        .param(esquema)
        .update();
  }

  public Optional<String> estadoDe(String esquema) {
    return jdbc.sql("SELECT estado FROM catalogo.migracion_tenant WHERE esquema = ?")
        .param(esquema)
        .query(String.class)
        .optional();
  }

  public void marcarAplicada(String esquema, String version) {
    jdbc.sql(
            "UPDATE catalogo.migracion_tenant SET estado = 'aplicada', version_aplicada = ?,"
                + " ultimo_intento = now(), actualizado_en = now() WHERE esquema = ?")
        .param(version)
        .param(esquema)
        .update();
  }

  public void marcarFallida(String esquema) {
    jdbc.sql(
            "UPDATE catalogo.migracion_tenant SET estado = 'fallida', ultimo_intento = now(),"
                + " actualizado_en = now() WHERE esquema = ?")
        .param(esquema)
        .update();
  }

  /** Quita el registro de un esquema; solo para deshacer un alta fallida. */
  public void eliminar(String esquema) {
    jdbc.sql("DELETE FROM catalogo.migracion_tenant WHERE esquema = ?").param(esquema).update();
  }

  /** Esquemas pendientes o fallidos cuyo último movimiento es anterior a la antigüedad dada. */
  public List<String> sinMigrarDesde(Duration antiguedad) {
    return jdbc.sql(
            "SELECT esquema FROM catalogo.migracion_tenant WHERE estado IN ('pendiente', 'fallida')"
                + " AND actualizado_en < now() - make_interval(secs => ?) ORDER BY esquema")
        .param(antiguedad.toSeconds())
        .query(String.class)
        .list();
  }
}
