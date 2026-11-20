package com.vetcat.tenancy;

/** No hay tenant en la operación: sin tenant no hay esquema que resolver. */
public class TenantAusenteException extends IllegalStateException {

  public TenantAusenteException() {
    super("No hay una clínica activa en la operación");
  }
}
