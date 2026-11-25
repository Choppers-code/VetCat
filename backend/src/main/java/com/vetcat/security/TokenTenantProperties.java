package com.vetcat.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del token de tenant que emite la API (ADR-0003).
 *
 * <p>La clave llega por entorno (`VETCAT_TOKEN_CLAVE`) y no tiene valor por defecto: sin ella la
 * API no arranca, igual que sin la contraseña de la base.
 *
 * @param emisor valor de `iss` del token; el decodificador solo acepta este
 * @param vigencia vida del token, 15 minutos por defecto
 * @param clave clave HMAC de al menos 32 bytes
 * @param kid identificador de la clave, para poder rotarla sin cortar sesiones
 */
@ConfigurationProperties("vetcat.token")
public record TokenTenantProperties(String emisor, Duration vigencia, String clave, String kid) {

  public TokenTenantProperties {
    emisor = emisor == null || emisor.isBlank() ? "vetcat-api" : emisor;
    vigencia =
        vigencia == null || vigencia.isNegative() || vigencia.isZero()
            ? Duration.ofMinutes(15)
            : vigencia;
    kid = kid == null || kid.isBlank() ? "dev-1" : kid;
    if (clave == null || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
      throw new IllegalArgumentException(
          "Falta VETCAT_TOKEN_CLAVE o mide menos de 32 bytes: la API no puede firmar el token de tenant");
    }
  }
}
