package com.vetcat.propietarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.vetcat.plataforma.dto.AltaClinicaRequest;
import com.vetcat.plataforma.dto.ClinicaResponse;
import com.vetcat.plataforma.service.AprovisionamientoClinicaService;
import com.vetcat.plataforma.service.RegistroAdministrador;
import com.vetcat.security.TokenTenantService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Propietarios de punta a punta con dos clínicas reales (PRO-001 y PRO-002): aislamiento,
 * autorización por rol, duplicados por teléfono y validación de entrada.
 */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.migraciones.tenant=filesystem:../database/migrations/tenant",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac",
      "vetcat.identidad.emisor=http://keycloak-de-prueba/realms/vetcat",
      "vetcat.identidad.jwk-set-uri=http://127.0.0.1:1/certs"
    })
@AutoConfigureMockMvc
class PropietariosTest {

  private static final String RUTA = "/api/v1/propietarios";

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  /** Las pruebas no dependen de Keycloak. */
  @TestConfiguration
  static class AdministradorDePrueba {
    @Bean
    @Primary
    RegistroAdministrador registroAdministrador() {
      return (clinicaId, nombre, correo) -> UUID.randomUUID();
    }
  }

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void migraElCatalogo() throws SQLException {
    ejecuta("CREATE SCHEMA catalogo");
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();
  }

  private static ClinicaResponse clinicaA;
  private static ClinicaResponse clinicaB;

  @Autowired MockMvc mockMvc;
  @Autowired TokenTenantService tokens;
  @Autowired AprovisionamientoClinicaService aprovisionamiento;

  @BeforeEach
  void preparaDosClinicasVacias() throws SQLException {
    if (clinicaA == null) {
      clinicaA = aprovisionamiento.aprovisionar(alta("Clínica A", "a@vetcat.dev"));
      clinicaB = aprovisionamiento.aprovisionar(alta("Clínica B", "b@vetcat.dev"));
    }
    for (ClinicaResponse clinica : List.of(clinicaA, clinicaB)) {
      ejecuta(
          "TRUNCATE %1$s.mascota, %1$s.propietario RESTART IDENTITY CASCADE"
              .formatted(clinica.esquema()));
    }
  }

  private static AltaClinicaRequest alta(String nombre, String correo) {
    return new AltaClinicaRequest(nombre, correo, null, "America/Mexico_City", "Ana", correo);
  }

  private static void ejecuta(String sql) throws SQLException {
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement()) {
      s.execute(sql);
    }
  }

  private static long cuenta(ClinicaResponse clinica) throws SQLException {
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement();
        ResultSet rs =
            s.executeQuery("SELECT count(*) FROM " + clinica.esquema() + ".propietario")) {
      rs.next();
      return rs.getLong(1);
    }
  }

  private ResultActions como(
      String rol, ClinicaResponse clinica, MockHttpServletRequestBuilder peticion)
      throws Exception {
    String token = tokens.emitir("usuario-1", clinica.id(), List.of(rol));
    return mockMvc.perform(peticion.header("Authorization", "Bearer " + token));
  }

  private static MockHttpServletRequestBuilder conCuerpo(
      MockHttpServletRequestBuilder peticion, String cuerpo) {
    return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
  }

  private static String datos(String nombre, String telefono) {
    return """
        {"nombre": "%s", "telefono": "%s"}"""
        .formatted(nombre, telefono);
  }

  private long registra(ClinicaResponse clinica, String nombre, String telefono) throws Exception {
    String respuesta =
        como("recepcionista", clinica, conCuerpo(post(RUTA), datos(nombre, telefono)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return ((Number) JsonPath.read(respuesta, "$.data.id")).longValue();
  }

  @Test
  void registra_un_propietario_y_su_ficha_no_tiene_mascotas() throws Exception {
    long id = registra(clinicaA, "María Pérez", "55 1234 5678");

    como("veterinario", clinicaA, get(RUTA + "/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.nombre").value("María Pérez"))
        .andExpect(jsonPath("$.data.telefono").value("55 1234 5678"))
        .andExpect(jsonPath("$.data.activo").value(true))
        .andExpect(jsonPath("$.data.correo").doesNotExist())
        .andExpect(jsonPath("$.data.mascotas").isEmpty());
  }

  @Test
  void la_ficha_muestra_solo_las_mascotas_activas_del_propietario() throws Exception {
    long ana = registra(clinicaA, "Ana", "5511111111");
    long beto = registra(clinicaA, "Beto", "5522222222");
    ejecuta(
        "INSERT INTO %s.mascota (propietario_id, nombre, especie, activo) VALUES "
                .formatted(clinicaA.esquema())
            + "(%1$d, 'Luna', 'GATO', true), (%1$d, 'Baja', 'PERRO', false), ".formatted(ana)
            + "(%d, 'Ajena', 'AVE', true)".formatted(beto));

    como("recepcionista", clinicaA, get(RUTA + "/" + ana))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.mascotas.length()").value(1))
        .andExpect(jsonPath("$.data.mascotas[0].nombre").value("Luna"))
        .andExpect(jsonPath("$.data.mascotas[0].propietarioId").value(ana));
  }

  @Test
  void edita_los_datos_de_un_propietario() throws Exception {
    long id = registra(clinicaA, "Ana", "5511111111");
    String cuerpo =
        """
        {"nombre": "Ana López", "telefono": "5533333333", "correo": "ana@correo.mx"}""";

    como("clinica_admin", clinicaA, conCuerpo(put(RUTA + "/" + id), cuerpo))
        .andExpect(status().isOk());

    como("clinica_admin", clinicaA, get(RUTA + "/" + id))
        .andExpect(jsonPath("$.data.nombre").value("Ana López"))
        .andExpect(jsonPath("$.data.telefono").value("5533333333"))
        .andExpect(jsonPath("$.data.correo").value("ana@correo.mx"));
  }

  @Test
  void un_propietario_de_otra_clinica_es_no_encontrado_y_no_se_modifica() throws Exception {
    long id = registra(clinicaA, "Ana", "5511111111");

    como("clinica_admin", clinicaB, get(RUTA + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist());
    como("clinica_admin", clinicaB, conCuerpo(put(RUTA + "/" + id), datos("Intruso", "5599999999")))
        .andExpect(status().isNotFound());

    como("clinica_admin", clinicaA, get(RUTA + "/" + id))
        .andExpect(jsonPath("$.data.nombre").value("Ana"));
    assertThat(cuenta(clinicaB)).as("la otra clínica sigue sin propietarios").isZero();
  }

  @Test
  void el_mismo_telefono_en_otra_clinica_no_es_duplicado() throws Exception {
    registra(clinicaA, "Ana", "5511111111");
    registra(clinicaB, "Ana", "5511111111");

    assertThat(cuenta(clinicaA)).isEqualTo(1);
    assertThat(cuenta(clinicaB)).isEqualTo(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"clinica_admin", "recepcionista", "veterinario"})
  void el_personal_registra_consulta_y_edita(String rol) throws Exception {
    como(rol, clinicaA, conCuerpo(post(RUTA), datos("Ana", "5511111111")))
        .andExpect(status().isCreated());
    como(rol, clinicaA, get(RUTA + "/1")).andExpect(status().isOk());
    como(rol, clinicaA, conCuerpo(put(RUTA + "/1"), datos("Ana María", "5511111111")))
        .andExpect(status().isOk());
  }

  @Test
  void el_cliente_recibe_403_y_no_cambia_nada() throws Exception {
    long id = registra(clinicaA, "Ana", "5511111111");

    como("cliente", clinicaA, conCuerpo(post(RUTA), datos("Beto", "5522222222")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
    como("cliente", clinicaA, get(RUTA + "/" + id)).andExpect(status().isForbidden());
    como("cliente", clinicaA, conCuerpo(put(RUTA + "/" + id), datos("Otro", "5511111111")))
        .andExpect(status().isForbidden());

    assertThat(cuenta(clinicaA)).isEqualTo(1);
  }

  @Test
  void sin_token_responde_401() throws Exception {
    mockMvc
        .perform(conCuerpo(post(RUTA), datos("Ana", "5511111111")))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get(RUTA + "/1")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(conCuerpo(put(RUTA + "/1"), datos("Ana", "5511111111")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void un_telefono_repetido_da_409_hasta_que_se_confirma() throws Exception {
    registra(clinicaA, "Ana", "(55) 1234-5678");

    como("recepcionista", clinicaA, conCuerpo(post(RUTA), datos("Beto", "55 1234 5678")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
    assertThat(cuenta(clinicaA)).isEqualTo(1);

    String confirmado =
        """
        {"nombre": "Beto", "telefono": "55 1234 5678", "confirmarDuplicado": true}""";
    como("recepcionista", clinicaA, conCuerpo(post(RUTA), confirmado))
        .andExpect(status().isCreated());
    assertThat(cuenta(clinicaA)).isEqualTo(2);
  }

  @Test
  void dos_altas_simultaneas_del_mismo_telefono_crean_una_sola() throws Exception {
    ExecutorService ejecutor = Executors.newFixedThreadPool(2);
    try {
      for (int vuelta = 0; vuelta < 10; vuelta++) {
        String cuerpo = datos("Ana", "55000000%02d".formatted(vuelta));
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Integer>> altas = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
          altas.add(
              ejecutor.submit(
                  () -> {
                    salida.await();
                    return como("recepcionista", clinicaA, conCuerpo(post(RUTA), cuerpo))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                  }));
        }
        salida.countDown();

        assertThat(
                List.of(
                    altas.get(0).get(30, TimeUnit.SECONDS), altas.get(1).get(30, TimeUnit.SECONDS)))
            .as("una alta gana y la otra recibe conflicto")
            .containsExactlyInAnyOrder(201, 409);
      }
    } finally {
      ejecutor.shutdownNow();
    }
    assertThat(cuenta(clinicaA)).isEqualTo(10);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"telefono\": \"5511111111\"}",
        "{\"nombre\": \"   \", \"telefono\": \"5511111111\"}",
        "{\"nombre\": \"Ana\"}",
        "{\"nombre\": \"Ana\", \"telefono\": \"sin número\"}",
        "{\"nombre\": \"Ana\", \"telefono\": \"1234567890123456789012345678901\"}",
        "{\"nombre\": \"Ana\", \"telefono\": \"5511111111\", \"correo\": \"no-es-correo\"}",
        "{\"nombre\": "
      })
  void una_entrada_invalida_responde_422_con_el_sobre(String cuerpo) throws Exception {
    como("recepcionista", clinicaA, conCuerpo(post(RUTA), cuerpo))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").isNotEmpty());

    assertThat(cuenta(clinicaA)).isZero();
  }

  @Test
  void la_direccion_se_rechaza_mientras_no_se_verifique_el_plan() throws Exception {
    String cuerpo =
        """
        {"nombre": "Ana", "telefono": "5511111111", "direccion": "Calle 1"}""";

    como("recepcionista", clinicaA, conCuerpo(post(RUTA), cuerpo)).andExpect(status().is(422));

    assertThat(cuenta(clinicaA)).isZero();
  }
}
