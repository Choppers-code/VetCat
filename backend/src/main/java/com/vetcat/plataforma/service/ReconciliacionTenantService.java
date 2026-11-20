package com.vetcat.plataforma.service;

import com.vetcat.plataforma.repository.MigracionTenantRepository;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Reintenta las migraciones de tenant que quedaron pendientes o fallidas (ADR-0010).
 *
 * <p>Recibe el esquema de forma explícita desde el catálogo; nunca lo hereda del hilo. La
 * antigüedad evita pisar un alta que sigue en curso.
 */
@Service
public class ReconciliacionTenantService {

  private static final Logger LOG = LoggerFactory.getLogger(ReconciliacionTenantService.class);

  private final MigracionTenantRepository estados;
  private final MigradorTenant migrador;

  public ReconciliacionTenantService(MigracionTenantRepository estados, MigradorTenant migrador) {
    this.estados = estados;
    this.migrador = migrador;
  }

  /** Devuelve cuántos esquemas quedaron aplicados en esta pasada. */
  public int reconciliar(Duration antiguedad) {
    int reparados = 0;
    for (String esquema : estados.sinMigrarDesde(antiguedad)) {
      try {
        migrador.aplicar(esquema);
        reparados++;
      } catch (RuntimeException e) {
        // Un esquema que falla no impide reintentar los demás; queda marcado como fallido.
        LOG.warn("La reconciliación no pudo migrar un esquema; se reintentará", e);
      }
    }
    return reparados;
  }
}
