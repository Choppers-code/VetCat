package com.vetcat.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Entrega a Hibernate el tenant de la operación en curso.
 *
 * <p>Falla cerrada: sin tenant lanza una excepción. Nunca devuelve un esquema por defecto, porque
 * el silencio significaría leer datos de otra clínica (docs/Decisiones_tecnicas.md §2).
 */
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver<Long> {

  @Override
  public Long resolveCurrentTenantIdentifier() {
    return TenantContext.actual().orElseThrow(TenantAusenteException::new);
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
