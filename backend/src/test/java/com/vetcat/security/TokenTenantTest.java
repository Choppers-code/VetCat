package com.vetcat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/** El token de tenant: claims, caducidad y firma. Sin base de datos ni contexto de Spring. */
class TokenTenantTest {

  private static final String CLAVE = "clave-de-prueba-con-mas-de-32-bytes-para-hmac-sha256";
  private static final String OTRA_CLAVE = "otra-clave-de-prueba-con-mas-de-32-bytes-para-hmac";
  private static final Instant AHORA = Instant.parse("2026-11-24T10:00:00Z");
  private static final String USUARIO = "f1c2d3e4-0000-4000-8000-000000000001";

  private TokenTenantProperties propiedades;
  private JwtEncoder codificador;
  private TokenTenantService servicio;

  @BeforeEach
  void prepara() {
    propiedades = new TokenTenantProperties("vetcat-api", Duration.ofMinutes(15), CLAVE, "dev-1");
    TokenTenantConfig configuracion = new TokenTenantConfig();
    codificador = configuracion.codificadorDeTokenTenant(propiedades);
    servicio =
        new TokenTenantService(
            propiedades,
            codificador,
            configuracion.decodificadorDeTokenTenant(propiedades),
            Clock.fixed(AHORA, ZoneOffset.UTC));
  }

  @Test
  void emite_el_subject_la_clinica_y_los_roles() {
    String token = servicio.emitir(USUARIO, 42L, List.of("veterinario", "clinica_admin"));

    TokenTenant leido = servicio.validar(token);

    assertThat(leido.sujeto()).isEqualTo(USUARIO);
    assertThat(leido.clinicaId()).isEqualTo(42L);
    assertThat(leido.roles()).containsExactly("veterinario", "clinica_admin");
  }

  @Test
  void el_token_dura_quince_minutos() throws Exception {
    SignedJWT jwt = SignedJWT.parse(servicio.emitir(USUARIO, 1L, List.of("cliente")));

    long segundos =
        jwt.getJWTClaimsSet().getExpirationTime().toInstant().getEpochSecond()
            - jwt.getJWTClaimsSet().getIssueTime().toInstant().getEpochSecond();

    assertThat(segundos).isEqualTo(15 * 60);
  }

  @Test
  void el_token_lleva_kid_para_poder_rotar_la_clave() throws Exception {
    SignedJWT jwt = SignedJWT.parse(servicio.emitir(USUARIO, 1L, List.of("cliente")));

    assertThat(jwt.getHeader().getKeyID()).isEqualTo("dev-1");
  }

  @Test
  void cada_token_lleva_un_jti_distinto() throws Exception {
    SignedJWT uno = SignedJWT.parse(servicio.emitir(USUARIO, 1L, List.of("cliente")));
    SignedJWT otro = SignedJWT.parse(servicio.emitir(USUARIO, 1L, List.of("cliente")));

    assertThat(uno.getJWTClaimsSet().getJWTID())
        .isNotBlank()
        .isNotEqualTo(otro.getJWTClaimsSet().getJWTID());
  }

  @Test
  void rechaza_un_token_firmado_con_otra_clave() {
    TokenTenantProperties otras =
        new TokenTenantProperties("vetcat-api", Duration.ofMinutes(15), OTRA_CLAVE, "dev-1");
    TokenTenantConfig configuracion = new TokenTenantConfig();
    TokenTenantService ajeno =
        new TokenTenantService(
            otras,
            configuracion.codificadorDeTokenTenant(otras),
            configuracion.decodificadorDeTokenTenant(otras),
            Clock.fixed(AHORA, ZoneOffset.UTC));
    String token = ajeno.emitir(USUARIO, 1L, List.of("cliente"));

    assertThatThrownBy(() -> servicio.validar(token))
        .isInstanceOf(TokenTenantInvalidoException.class);
  }

  @Test
  void rechaza_un_token_de_otro_emisor() {
    TokenTenantProperties otroEmisorProps =
        new TokenTenantProperties("otra-api", Duration.ofMinutes(15), CLAVE, "dev-1");
    TokenTenantConfig configuracion = new TokenTenantConfig();
    TokenTenantService otroEmisor =
        new TokenTenantService(
            otroEmisorProps,
            configuracion.codificadorDeTokenTenant(otroEmisorProps),
            configuracion.decodificadorDeTokenTenant(otroEmisorProps),
            Clock.fixed(AHORA, ZoneOffset.UTC));
    String token = otroEmisor.emitir(USUARIO, 1L, List.of("cliente"));

    assertThatThrownBy(() -> servicio.validar(token))
        .isInstanceOf(TokenTenantInvalidoException.class);
  }

  @Test
  void rechaza_un_token_vencido() {
    TokenTenantService antiguo =
        new TokenTenantService(
            propiedades,
            codificador,
            new TokenTenantConfig().decodificadorDeTokenTenant(propiedades),
            Clock.fixed(Instant.parse("2020-01-01T10:00:00Z"), ZoneOffset.UTC));
    String token = antiguo.emitir(USUARIO, 1L, List.of("cliente"));

    assertThatThrownBy(() -> servicio.validar(token))
        .isInstanceOf(TokenTenantInvalidoException.class);
  }

  @Test
  void rechaza_un_token_sin_clinica() throws Exception {
    JwtClaimsSet sinClinica =
        JwtClaimsSet.builder()
            .issuer(propiedades.emisor())
            .subject(USUARIO)
            .issuedAt(AHORA)
            .expiresAt(AHORA.plus(Duration.ofMinutes(15)))
            .id(UUID.randomUUID().toString())
            .claim("roles", List.of("cliente"))
            .build();
    JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).keyId(propiedades.kid()).build();
    String token =
        codificador.encode(JwtEncoderParameters.from(cabecera, sinClinica)).getTokenValue();

    assertThatThrownBy(() -> servicio.validar(token))
        .isInstanceOf(TokenTenantInvalidoException.class);
  }

  @Test
  void no_emite_un_token_sin_roles() {
    assertThatThrownBy(() -> servicio.emitir(USUARIO, 1L, List.of()))
        .isInstanceOf(TokenTenantInvalidoException.class);
  }
}
