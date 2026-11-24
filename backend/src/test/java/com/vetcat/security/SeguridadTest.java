package com.vetcat.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * La prioridad 2 del proyecto: 401 sin token y 403 con el rol equivocado, con el sobre de error.
 *
 * <p>Los dos emisores están enrutados por cadena: el token de identidad no abre un endpoint de
 * negocio y el de tenant no sirve para abrir sesión.
 */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac",
      "vetcat.identidad.emisor=http://keycloak-de-prueba/realms/vetcat",
      "vetcat.identidad.jwk-set-uri=http://127.0.0.1:1/certs"
    })
@AutoConfigureMockMvc
class SeguridadTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @org.junit.jupiter.api.BeforeAll
  static void migraElCatalogo() throws java.sql.SQLException {
    try (java.sql.Connection c = POSTGRES.createConnection("");
        java.sql.Statement s = c.createStatement()) {
      s.execute("CREATE SCHEMA catalogo");
    }
    org.flywaydb.core.Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();
  }

  @Autowired MockMvc mockMvc;
  @Autowired TokenTenantService tokens;

  @Test
  void sin_token_responde_401_con_el_sobre() throws Exception {
    mockMvc
        .perform(get("/api/v1/prueba"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void un_rol_ajeno_responde_403_con_el_sobre() throws Exception {
    String token = tokens.emitir("usuario-1", 1L, List.of("cliente"));

    mockMvc
        .perform(get("/api/v1/prueba").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void el_rol_correcto_entra() throws Exception {
    String token = tokens.emitir("usuario-1", 1L, List.of("veterinario"));

    mockMvc
        .perform(get("/api/v1/prueba").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void el_token_de_identidad_no_abre_un_endpoint_de_negocio() throws Exception {
    TokenTenantProperties otroEmisor =
        new TokenTenantProperties(
            "http://keycloak-de-prueba/realms/vetcat",
            java.time.Duration.ofMinutes(15),
            "clave-de-prueba-con-mas-de-32-bytes-para-hmac",
            "dev-1");
    TokenTenantConfig configuracion = new TokenTenantConfig();
    TokenTenantService identidad =
        new TokenTenantService(
            otroEmisor,
            configuracion.codificadorDeTokenTenant(otroEmisor),
            configuracion.decodificadorDeTokenTenant(otroEmisor),
            Clock.systemUTC());
    String token = identidad.emitir("usuario-1", 1L, List.of("super_admin"));

    mockMvc
        .perform(get("/api/v1/prueba").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void el_token_de_tenant_no_abre_la_sesion() throws Exception {
    String token = tokens.emitir("usuario-1", 1L, List.of("veterinario"));

    mockMvc
        .perform(get("/api/v1/sesion/clinicas").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }
}
