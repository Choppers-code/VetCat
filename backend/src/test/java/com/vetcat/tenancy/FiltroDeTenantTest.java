package com.vetcat.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vetcat.security.TokenTenantConfig;
import com.vetcat.security.TokenTenantProperties;
import com.vetcat.security.TokenTenantService;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
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
 * El tenant de la petición sale del token y se limpia al terminar.
 *
 * <p>Prioridad 1 del proyecto: sin tenant no hay esquema, y un tenant filtrado cruzaría clínicas.
 */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba,com.vetcat.citas.entity",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac",
      "vetcat.identidad.emisor=http://keycloak-de-prueba/realms/vetcat",
      "vetcat.identidad.jwk-set-uri=http://127.0.0.1:1/certs"
    })
@AutoConfigureMockMvc
class FiltroDeTenantTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
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
  void el_tenant_del_token_queda_en_el_contexto() throws Exception {
    String token = tokens.emitir("usuario-1", 7L, List.of("veterinario"));

    mockMvc
        .perform(get("/api/v1/prueba-tenant").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(content().string("7"));
  }

  @Test
  void cada_token_ve_su_clinica() throws Exception {
    String deSiete = tokens.emitir("usuario-1", 7L, List.of("veterinario"));
    String deNueve = tokens.emitir("usuario-2", 9L, List.of("clinica_admin"));

    mockMvc
        .perform(get("/api/v1/prueba-tenant").header("Authorization", "Bearer " + deSiete))
        .andExpect(content().string("7"));
    mockMvc
        .perform(get("/api/v1/prueba-tenant").header("Authorization", "Bearer " + deNueve))
        .andExpect(content().string("9"));
  }

  @Test
  void sin_token_responde_401() throws Exception {
    mockMvc.perform(get("/api/v1/prueba-tenant")).andExpect(status().isUnauthorized());
  }

  @Test
  void el_token_de_identidad_no_pone_tenant() throws Exception {
    TokenTenantProperties identidad =
        new TokenTenantProperties(
            "http://keycloak-de-prueba/realms/vetcat",
            java.time.Duration.ofMinutes(15),
            "clave-de-prueba-con-mas-de-32-bytes-para-hmac",
            "dev-1");
    TokenTenantConfig configuracion = new TokenTenantConfig();
    TokenTenantService emisorDeIdentidad =
        new TokenTenantService(
            identidad,
            configuracion.codificadorDeTokenTenant(identidad),
            configuracion.decodificadorDeTokenTenant(identidad),
            Clock.systemUTC());
    String token = emisorDeIdentidad.emitir("usuario-1", 7L, List.of("super_admin"));

    mockMvc
        .perform(get("/api/v1/prueba-tenant").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void el_contexto_queda_limpio_al_terminar() throws Exception {
    String token = tokens.emitir("usuario-1", 7L, List.of("veterinario"));

    mockMvc.perform(get("/api/v1/prueba-tenant").header("Authorization", "Bearer " + token));

    assertThat(TenantContext.actual()).isEmpty();
  }
}
