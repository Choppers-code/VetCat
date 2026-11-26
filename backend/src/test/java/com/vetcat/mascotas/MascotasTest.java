package com.vetcat.mascotas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
 * Mascotas de punta a punta con dos clínicas reales (PRO-005, PRO-006 y PRO-008): aislamiento,
 * autorización por rol, validación y propietario inmutable.
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
class MascotasTest {

  private static final String RUTA = "/api/v1/mascotas";

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

  /** Cada clínica empieza con un propietario, el 1, y sin mascotas. */
  @BeforeEach
  void preparaDosClinicas() throws SQLException {
    if (clinicaA == null) {
      clinicaA = aprovisionamiento.aprovisionar(alta("Clínica A", "a@vetcat.dev"));
      clinicaB = aprovisionamiento.aprovisionar(alta("Clínica B", "b@vetcat.dev"));
    }
    for (ClinicaResponse clinica : List.of(clinicaA, clinicaB)) {
      ejecuta(
          "TRUNCATE %1$s.mascota, %1$s.propietario RESTART IDENTITY CASCADE"
              .formatted(clinica.esquema()));
      ejecuta(
          "INSERT INTO %s.propietario (nombre, telefono) VALUES ('Ana', '5511111111')"
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

  private static String valor(String sql) throws SQLException {
    try (Connection c = POSTGRES.createConnection("");
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(sql)) {
      rs.next();
      return rs.getString(1);
    }
  }

  private static long cuenta(ClinicaResponse clinica) throws SQLException {
    return Long.parseLong(valor("SELECT count(*) FROM " + clinica.esquema() + ".mascota"));
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

  private static String nueva(long propietarioId, String nombre) {
    return """
        {"propietarioId": %d, "nombre": "%s", "especie": "GATO"}"""
        .formatted(propietarioId, nombre);
  }

  private static String cambios(String nombre) {
    return """
        {"nombre": "%s", "especie": "PERRO"}"""
        .formatted(nombre);
  }

  private long registra(ClinicaResponse clinica, String nombre) throws Exception {
    String respuesta =
        como("veterinario", clinica, conCuerpo(post(RUTA), nueva(1, nombre)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return ((Number) JsonPath.read(respuesta, "$.data.id")).longValue();
  }

  @Test
  void registra_una_mascota_con_todos_sus_datos_y_aparece_en_la_ficha_del_propietario()
      throws Exception {
    String cuerpo =
        """
        {"propietarioId": 1, "nombre": "Luna", "especie": "GATO", "raza": "Siamés",
         "sexo": "HEMBRA", "fechaNacimiento": "2020-05-17", "pesoKg": 4.25, "notas": "Tímida"}""";

    como("recepcionista", clinicaA, conCuerpo(post(RUTA), cuerpo)).andExpect(status().isCreated());

    como("recepcionista", clinicaA, get(RUTA + "/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.propietarioId").value(1))
        .andExpect(jsonPath("$.data.nombre").value("Luna"))
        .andExpect(jsonPath("$.data.especie").value("GATO"))
        .andExpect(jsonPath("$.data.raza").value("Siamés"))
        .andExpect(jsonPath("$.data.sexo").value("HEMBRA"))
        .andExpect(jsonPath("$.data.fechaNacimiento").value("2020-05-17"))
        .andExpect(jsonPath("$.data.pesoKg").value(4.25))
        .andExpect(jsonPath("$.data.notas").value("Tímida"))
        .andExpect(jsonPath("$.data.activo").value(true));
    como("recepcionista", clinicaA, get("/api/v1/propietarios/1"))
        .andExpect(jsonPath("$.data.mascotas[0].nombre").value("Luna"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"clinica_admin", "recepcionista", "veterinario"})
  void el_personal_registra_consulta_y_edita(String rol) throws Exception {
    como(rol, clinicaA, conCuerpo(post(RUTA), nueva(1, "Luna"))).andExpect(status().isCreated());
    como(rol, clinicaA, get(RUTA + "/1")).andExpect(status().isOk());
    como(rol, clinicaA, conCuerpo(put(RUTA + "/1"), cambios("Sol")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nombre").value("Sol"))
        .andExpect(jsonPath("$.data.especie").value("PERRO"))
        .andExpect(jsonPath("$.data.propietarioId").value(1));
  }

  @Test
  void el_cliente_recibe_403_y_no_cambia_nada() throws Exception {
    registra(clinicaA, "Luna");

    como("cliente", clinicaA, conCuerpo(post(RUTA), nueva(1, "Otra")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
    como("cliente", clinicaA, get(RUTA + "/1")).andExpect(status().isForbidden());
    como("cliente", clinicaA, conCuerpo(put(RUTA + "/1"), cambios("Otra")))
        .andExpect(status().isForbidden());

    assertThat(cuenta(clinicaA)).isEqualTo(1);
    assertThat(valor("SELECT nombre FROM " + clinicaA.esquema() + ".mascota")).isEqualTo("Luna");
  }

  @Test
  void sin_token_responde_401() throws Exception {
    mockMvc.perform(conCuerpo(post(RUTA), nueva(1, "Luna"))).andExpect(status().isUnauthorized());
    mockMvc.perform(get(RUTA + "/1")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(conCuerpo(put(RUTA + "/1"), cambios("Sol")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void una_mascota_de_otra_clinica_es_no_encontrada_y_no_se_modifica() throws Exception {
    long id = registra(clinicaA, "Luna");

    como("clinica_admin", clinicaB, get(RUTA + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
    como("clinica_admin", clinicaB, conCuerpo(put(RUTA + "/" + id), cambios("Intrusa")))
        .andExpect(status().isNotFound());

    assertThat(valor("SELECT nombre FROM " + clinicaA.esquema() + ".mascota")).isEqualTo("Luna");
    assertThat(cuenta(clinicaB)).isZero();
  }

  @Test
  void no_se_registra_una_mascota_para_un_propietario_de_otra_clinica() throws Exception {
    ejecuta(
        "INSERT INTO %s.propietario (nombre, telefono) VALUES ('Beto', '5522222222')"
            .formatted(clinicaA.esquema()));

    // El propietario 2 solo existe en la clínica A.
    como("clinica_admin", clinicaB, conCuerpo(post(RUTA), nueva(2, "Luna")))
        .andExpect(status().isNotFound());

    assertThat(cuenta(clinicaA)).isZero();
    assertThat(cuenta(clinicaB)).isZero();
  }

  @Test
  void no_se_registra_una_mascota_para_un_propietario_inactivo() throws Exception {
    ejecuta("UPDATE " + clinicaA.esquema() + ".propietario SET activo = false");

    como("veterinario", clinicaA, conCuerpo(post(RUTA), nueva(1, "Luna")))
        .andExpect(status().isNotFound());

    assertThat(cuenta(clinicaA)).isZero();
  }

  @Test
  void el_alta_espera_a_una_baja_en_curso_y_no_deja_una_mascota_activa_bajo_un_inactivo()
      throws Exception {
    ExecutorService ejecutor = Executors.newSingleThreadExecutor();
    try (Connection baja = POSTGRES.createConnection("");
        Statement s = baja.createStatement()) {
      baja.setAutoCommit(false);
      s.execute("UPDATE " + clinicaA.esquema() + ".propietario SET activo = false");

      Future<Integer> alta =
          ejecutor.submit(
              () ->
                  como("veterinario", clinicaA, conCuerpo(post(RUTA), nueva(1, "Luna")))
                      .andReturn()
                      .getResponse()
                      .getStatus());
      assertThatThrownBy(() -> alta.get(2, TimeUnit.SECONDS))
          .as("el alta no puede decidir mientras la baja no termina")
          .isInstanceOf(TimeoutException.class);
      baja.commit();

      assertThat(alta.get(30, TimeUnit.SECONDS)).isEqualTo(404);
    } finally {
      ejecutor.shutdownNow();
    }
    assertThat(cuenta(clinicaA)).isZero();
  }

  @Test
  void no_se_puede_cambiar_el_propietario_al_editar() throws Exception {
    ejecuta(
        "INSERT INTO %s.propietario (nombre, telefono) VALUES ('Beto', '5522222222')"
            .formatted(clinicaA.esquema()));
    long id = registra(clinicaA, "Luna");
    String cuerpo =
        """
        {"nombre": "Luna", "especie": "GATO", "propietarioId": 2}""";

    como("clinica_admin", clinicaA, conCuerpo(put(RUTA + "/" + id), cuerpo))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.success").value(false));

    assertThat(valor("SELECT propietario_id FROM " + clinicaA.esquema() + ".mascota"))
        .isEqualTo("1");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "\"especie\": \"GATO\"",
        "\"nombre\": \" \", \"especie\": \"GATO\"",
        "\"nombre\": \"Luna\"",
        "\"nombre\": \"Luna\", \"especie\": \"DRAGON\"",
        "\"nombre\": \"Luna\", \"especie\": \"GATO\", \"sexo\": \"X\"",
        "\"nombre\": \"Luna\", \"especie\": \"GATO\", \"pesoKg\": 0",
        "\"nombre\": \"Luna\", \"especie\": \"GATO\", \"pesoKg\": -1",
        "\"nombre\": \"Luna\", \"especie\": \"GATO\", \"pesoKg\": 10000",
        "\"nombre\": \"Luna\", \"especie\": \"GATO\", \"pesoKg\": 4.255",
        "\"nombre\": \"Luna\", \"especie\": \"GATO\", \"fechaNacimiento\": \"no-es-fecha\""
      })
  void una_entrada_invalida_responde_422_al_registrar_y_al_editar(String campos) throws Exception {
    long id = registra(clinicaA, "Original");

    como("veterinario", clinicaA, conCuerpo(post(RUTA), "{\"propietarioId\": 1, " + campos + "}"))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.success").value(false));
    como("veterinario", clinicaA, conCuerpo(put(RUTA + "/" + id), "{" + campos + "}"))
        .andExpect(status().is(422));

    assertThat(cuenta(clinicaA)).isEqualTo(1);
    assertThat(valor("SELECT nombre FROM " + clinicaA.esquema() + ".mascota"))
        .isEqualTo("Original");
  }

  @Test
  void una_fecha_de_nacimiento_futura_se_rechaza() throws Exception {
    String cuerpo =
        """
        {"propietarioId": 1, "nombre": "Luna", "especie": "GATO", "fechaNacimiento": "%s"}"""
            .formatted(LocalDate.now().plusDays(2));

    como("veterinario", clinicaA, conCuerpo(post(RUTA), cuerpo))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.message").value("La fecha de nacimiento no puede ser futura"));

    assertThat(cuenta(clinicaA)).isZero();
  }

  @Test
  void sin_propietario_no_se_registra() throws Exception {
    como(
            "veterinario",
            clinicaA,
            conCuerpo(post(RUTA), "{\"nombre\": \"Luna\", \"especie\": \"GATO\"}"))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.message").value("El propietario es obligatorio"));
  }
}
