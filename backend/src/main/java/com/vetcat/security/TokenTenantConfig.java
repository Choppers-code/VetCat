package com.vetcat.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** Piezas del token de tenant: firma, validación y reloj. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TokenTenantProperties.class)
public class TokenTenantConfig {

  /**
   * Firma HMAC del token de tenant. La clave se guarda como {@code OctetSequenceKey} para poder
   * rotarla con el `kid` de la cabecera sin cambiar el código.
   */
  @Bean
  public JwtEncoder codificadorDeTokenTenant(TokenTenantProperties propiedades) {
    OctetSequenceKey clave =
        new OctetSequenceKey.Builder(claveDe(propiedades))
            .algorithm(JWSAlgorithm.HS256)
            .keyID(propiedades.kid())
            .build();
    JWKSource<SecurityContext> origen = new ImmutableJWKSet<>(new JWKSet(clave));
    return new NimbusJwtEncoder(origen);
  }

  /** Acepta solo tokens con la firma de esta API y con su `iss`. */
  @Bean
  public JwtDecoder decodificadorDeTokenTenant(TokenTenantProperties propiedades) {
    NimbusJwtDecoder decodificador =
        NimbusJwtDecoder.withSecretKey(claveDe(propiedades))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(propiedades.emisor()));
    return decodificador;
  }

  @Bean
  public Clock relojDelSistema() {
    return Clock.systemUTC();
  }

  private static SecretKeySpec claveDe(TokenTenantProperties propiedades) {
    return new SecretKeySpec(propiedades.clave().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }
}
