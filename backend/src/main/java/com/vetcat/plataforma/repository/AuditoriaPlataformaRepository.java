package com.vetcat.plataforma.repository;

import com.vetcat.plataforma.entity.AccionDePlataforma;
import com.vetcat.plataforma.entity.AuditoriaPlataforma;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Auditoría de plataforma: se escribe y se lee; no hay borrado ni edición.
 *
 * <p>Con {@code JdbcClient} y no con JPA porque la unidad del catálogo solo escanea sus propias
 * entidades y esto es una traza de infraestructura, no un dato de negocio.
 */
@Repository
public class AuditoriaPlataformaRepository {

  private static final String COLUMNAS =
      "id, actor_keycloak_id, accion, clinica_id, detalle, creada_en";

  private final JdbcClient jdbc;

  public AuditoriaPlataformaRepository(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  public void guardar(
      UUID actorKeycloakId, AccionDePlataforma accion, Long clinicaId, String detalle) {
    jdbc.sql(
            "INSERT INTO catalogo.auditoria_plataforma (actor_keycloak_id, accion, clinica_id, detalle)"
                + " VALUES (:actor, :accion, :clinica, :detalle)")
        .param("actor", actorKeycloakId)
        .param("accion", accion.name())
        .param("clinica", clinicaId)
        .param("detalle", detalle)
        .update();
  }

  public List<AuditoriaPlataforma> deClinica(Long clinicaId) {
    return jdbc.sql(
            "SELECT "
                + COLUMNAS
                + " FROM catalogo.auditoria_plataforma WHERE clinica_id = :clinica"
                + " ORDER BY creada_en DESC, id DESC")
        .param("clinica", clinicaId)
        .query(AuditoriaPlataformaRepository::mapea)
        .list();
  }

  public List<AuditoriaPlataforma> deCatalogo() {
    return jdbc.sql(
            "SELECT "
                + COLUMNAS
                + " FROM catalogo.auditoria_plataforma WHERE clinica_id IS NULL"
                + " ORDER BY creada_en DESC, id DESC")
        .query(AuditoriaPlataformaRepository::mapea)
        .list();
  }

  private static AuditoriaPlataforma mapea(java.sql.ResultSet fila, int numero)
      throws java.sql.SQLException {
    return new AuditoriaPlataforma(
        fila.getLong("id"),
        fila.getObject("actor_keycloak_id", UUID.class),
        AccionDePlataforma.valueOf(fila.getString("accion")),
        fila.getObject("clinica_id", Long.class),
        fila.getString("detalle"),
        fila.getTimestamp("creada_en").toInstant());
  }
}
