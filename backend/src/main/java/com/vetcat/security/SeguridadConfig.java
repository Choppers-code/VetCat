package com.vetcat.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Dos emisores, dos cadenas (ADR-0003).
 *
 * <p>El token de identidad de Keycloak solo abre `/api/v1/sesion/**` y `/api/v1/plataforma/**`.
 * Todo lo demás exige el token de tenant, porque sin `tenant_id` no hay esquema que resolver.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SeguridadConfig {

  /**
   * Valida el token de identidad contra el JWKS del realm. En pruebas y en local no se llama al
   * endpoint: solo se usa si llega un token con ese emisor.
   */
  @Bean
  public JwtDecoder decodificadorDeIdentidad(
      @Value("${vetcat.identidad.jwk-set-uri}") String jwkSetUri,
      @Value("${vetcat.identidad.emisor}") String emisor) {
    NimbusJwtDecoder decodificador = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(emisor));
    return decodificador;
  }

  /** Sesión y plataforma: se autentican con el token de identidad de Keycloak. */
  @Bean
  @Order(1)
  public SecurityFilterChain cadenaDeIdentidad(
      HttpSecurity http,
      @Qualifier("decodificadorDeIdentidad") JwtDecoder decodificador,
      ManejadorDeErroresDeSeguridad errores)
      throws Exception {
    http.securityMatcher("/api/v1/sesion/**", "/api/v1/plataforma/**")
        .authorizeHttpRequests(reglas -> reglas.anyRequest().authenticated())
        .oauth2ResourceServer(
            recurso ->
                recurso
                    .jwt(
                        jwt ->
                            jwt.decoder(decodificador)
                                .jwtAuthenticationConverter(ConversionDeRoles.deIdentidad()))
                    .authenticationEntryPoint(errores))
        .exceptionHandling(
            manejo -> manejo.authenticationEntryPoint(errores).accessDeniedHandler(errores))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sesion -> sesion.sessionCreationPolicy(STATELESS));
    return http.build();
  }

  /** Todo lo demás: exige el token de tenant, y con él los roles de esa clínica. */
  @Bean
  @Order(2)
  public SecurityFilterChain cadenaDeNegocio(
      HttpSecurity http,
      @Qualifier("decodificadorDeTokenTenant") JwtDecoder decodificador,
      ManejadorDeErroresDeSeguridad errores)
      throws Exception {
    http.authorizeHttpRequests(reglas -> reglas.anyRequest().authenticated())
        .oauth2ResourceServer(
            recurso ->
                recurso
                    .jwt(
                        jwt ->
                            jwt.decoder(decodificador)
                                .jwtAuthenticationConverter(ConversionDeRoles.deTenant()))
                    .authenticationEntryPoint(errores))
        .exceptionHandling(
            manejo -> manejo.authenticationEntryPoint(errores).accessDeniedHandler(errores))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sesion -> sesion.sessionCreationPolicy(STATELESS));
    return http.build();
  }
}
