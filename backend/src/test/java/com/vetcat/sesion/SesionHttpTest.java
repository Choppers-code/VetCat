package com.vetcat.sesion;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.sun.net.httpserver.HttpServer;
import com.vetcat.security.TokenTenantService;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Los endpoints de sesión de punta a punta, con el token de identidad validado de verdad.
 *
 * <p>Levanta un JWKS en el propio test para no depender de Keycloak: el decodificador de identidad
 * valida firma y emisor como en producción.
 */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba,com.vetcat.citas.entity",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"
    })
@AutoConfigureMockMvc
class SesionHttpTest {

  private static final String EMISOR = "http://prueba/realms/vetcat";
  private static final UUID CON_MEMBRESIA = UUID.randomUUID();
  private static final UUID CON_MEMBRESIA_INACTIVA = UUID.randomUUID();
  private static final UUID EN_CLINICA_SUSPENDIDA = UUID.randomUUID();

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static final RSAKey CLAVE;
  private static final HttpServer JWKS;
  private static Long clinicaActiva;
  private static Long clinicaSuspendida;

  static {
    try {
      CLAVE = new RSAKeyGenerator(2048).keyID("prueba").generate();
      JWKS = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      JWKS.createContext(
          "/certs",
          intercambio -> {
            byte[] cuerpo = new JWKSet(CLAVE.toPublicJWK()).toString().getBytes(UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(200, cuerpo.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
              salida.write(cuerpo);
            }
          });
      JWKS.start();
    } catch (Exception excepcion) {
      throw new IllegalStateException("No se pudo levantar el JWKS de prueba", excepcion);
    }
  }

  @DynamicPropertySource
  static void propiedades(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
    registro.add("vetcat.identidad.emisor", () -> EMISOR);
    registro.add(
        "vetcat.identidad.jwk-set-uri",
        () -> "http://127.0.0.1:" + JWKS.getAddress().getPort() + "/certs");
  }

  @BeforeAll
  static void migraYPreparaClinicas() throws SQLException {
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement()) {
      s.execute("CREATE SCHEMA catalogo");
    }
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();

    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement()) {
      clinicaActiva = insertaClinica(s, "Clínica Norte", "ACTIVA");
      clinicaSuspendida = insertaClinica(s, "Clínica Sur", "SUSPENDIDA");
      s.execute(membresia(CON_MEMBRESIA, clinicaActiva, "veterinario", "ACTIVO"));
      s.execute(membresia(CON_MEMBRESIA, clinicaActiva, "clinica_admin", "ACTIVO"));
      s.execute(membresia(CON_MEMBRESIA_INACTIVA, clinicaActiva, "recepcionista", "INACTIVO"));
      s.execute(membresia(EN_CLINICA_SUSPENDIDA, clinicaActiva, "veterinario", "ACTIVO"));
      s.execute(membresia(EN_CLINICA_SUSPENDIDA, clinicaSuspendida, "veterinario", "ACTIVO"));
    }
  }

  private static Long insertaClinica(Statement s, String nombre, String estado)
      throws SQLException {
    var rs =
        s.executeQuery(
            "INSERT INTO catalogo.clinica (nombre, zona_horaria, estado) VALUES ('"
                + nombre
                + "', 'UTC', '"
                + estado
                + "') RETURNING id");
    rs.next();
    return rs.getLong(1);
  }

  private static String membresia(UUID usuario, long clinica, String rol, String estado) {
    return "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol, estado) VALUES ('"
        + usuario
        + "', "
        + clinica
        + ", '"
        + rol
        + "', '"
        + estado
        + "')";
  }

  private static String tokenDeIdentidad(UUID usuario) {
    Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    JwtClaimsSet reclamaciones =
        JwtClaimsSet.builder()
            .issuer(EMISOR)
            .subject(usuario.toString())
            .issuedAt(ahora)
            .expiresAt(ahora.plus(5, ChronoUnit.MINUTES))
            .claim("realm_access", java.util.Map.of("roles", List.of("veterinario")))
            .build();
    JwsHeader cabecera =
        JwsHeader.with(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
            .keyId(CLAVE.getKeyID())
            .build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(CLAVE)))
        .encode(JwtEncoderParameters.from(cabecera, reclamaciones))
        .getTokenValue();
  }

  @Autowired MockMvc mockMvc;
  @Autowired TokenTenantService tokens;

  @Test
  void lista_las_clinicas_y_muestra_las_suspendidas_con_su_estado() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/sesion/clinicas")
                .header("Authorization", "Bearer " + tokenDeIdentidad(EN_CLINICA_SUSPENDIDA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[1].estado").value("SUSPENDIDA"));
  }

  @Test
  void abre_sesion_con_los_roles_de_esa_clinica() throws Exception {
    String respuesta =
        mockMvc
            .perform(
                post("/api/v1/sesion/clinicas/" + clinicaActiva)
                    .header("Authorization", "Bearer " + tokenDeIdentidad(CON_MEMBRESIA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String token = com.jayway.jsonpath.JsonPath.read(respuesta, "$.data.token");
    var leido = tokens.validar(token);
    assertThat(leido.sujeto()).isEqualTo(CON_MEMBRESIA.toString());
    assertThat(leido.clinicaId()).isEqualTo(clinicaActiva);
    assertThat(leido.roles()).containsExactly("clinica_admin", "veterinario");
  }

  @Test
  void una_membresia_inactiva_responde_403_con_el_sobre() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/sesion/clinicas/" + clinicaActiva)
                .header("Authorization", "Bearer " + tokenDeIdentidad(CON_MEMBRESIA_INACTIVA)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No tienes acceso a esta clínica"));
  }

  @Test
  void una_clinica_suspendida_responde_403_con_su_mensaje() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/sesion/clinicas/" + clinicaSuspendida)
                .header("Authorization", "Bearer " + tokenDeIdentidad(EN_CLINICA_SUSPENDIDA)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("La clínica no está disponible"));
  }

  @Test
  void sin_token_responde_401() throws Exception {
    mockMvc.perform(get("/api/v1/sesion/clinicas")).andExpect(status().isUnauthorized());
  }
}
