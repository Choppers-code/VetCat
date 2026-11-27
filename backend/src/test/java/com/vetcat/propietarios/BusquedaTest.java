package com.vetcat.propietarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.vetcat.plataforma.dto.AltaClinicaRequest;
import com.vetcat.plataforma.dto.ClinicaResponse;
import com.vetcat.plataforma.service.AprovisionamientoClinicaService;
import com.vetcat.plataforma.service.RegistroAdministrador;
import com.vetcat.security.TokenTenantService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Búsqueda de propietarios y mascotas con dos clínicas reales (PRO-009): aislamiento, autorización,
 * acentos, texto literal, inactivos, paginación y tiempo con volumen (SC-004).
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
class BusquedaTest {

  private static final String RUTA = "/api/v1/busqueda";

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

  /**
   * En la clínica A: 1 María Pérez (con Ñandú y la inactiva Pelusa), 2 José Núñez y 3 Mariana,
   * inactiva. La clínica B empieza vacía.
   */
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
    }
    ejecuta(
        """
        INSERT INTO %s.propietario (nombre, telefono, correo, activo) VALUES
          ('María Pérez', '(55) 1234-5678', 'maria@correo.mx', true),
          ('José Núñez', '5598765432', NULL, true),
          ('Mariana Baja', '5500000000', NULL, false)
        """
            .formatted(clinicaA.esquema()));
    ejecuta(
        """
        INSERT INTO %s.mascota (propietario_id, nombre, especie, activo) VALUES
          (1, 'Ñandú', 'AVE', true),
          (1, 'Pelusa', 'GATO', false)
        """
            .formatted(clinicaA.esquema()));
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

  private ResultActions como(
      String rol, ClinicaResponse clinica, MockHttpServletRequestBuilder peticion)
      throws Exception {
    String token = tokens.emitir("usuario-1", clinica.id(), List.of(rol));
    return mockMvc.perform(peticion.header("Authorization", "Bearer " + token));
  }

  /** Nombres hallados: el de la mascota si el resultado es de una mascota, o el del propietario. */
  private List<String> halla(String texto) throws Exception {
    return nombres(como("recepcionista", clinicaA, get(RUTA).param("q", texto)));
  }

  private static List<String> nombres(ResultActions respuesta) throws Exception {
    String json =
        respuesta.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    int total = JsonPath.<List<Object>>read(json, "$.data.contenido").size();
    return java.util.stream.IntStream.range(0, total)
        .mapToObj(
            i -> {
              String base = "$.data.contenido[" + i + "]";
              java.util.Map<String, Object> fila = JsonPath.read(json, base);
              return JsonPath.<String>read(
                  json,
                  base + (fila.containsKey("mascota") ? ".mascota" : ".propietario") + ".nombre");
            })
        .toList();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "maria",
        "PÉREZ",
        "perez",
        "ria pe",
        "maria@correo",
        "1234-5678",
        "12345678",
        "  maria  "
      })
  void encuentra_al_propietario_sin_distinguir_acentos_ni_mayusculas(String texto)
      throws Exception {
    assertThat(halla(texto)).containsExactly("María Pérez");
  }

  @Test
  void una_mascota_se_muestra_junto_a_su_propietario() throws Exception {
    como("veterinario", clinicaA, get(RUTA).param("q", "nandu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.contenido[0].mascota.nombre").value("Ñandú"))
        .andExpect(jsonPath("$.data.contenido[0].propietario.nombre").value("María Pérez"))
        .andExpect(jsonPath("$.data.contenido[0].propietario.mascotas.length()").value(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {"%%%", "___", "ma%ia", "ma_ia", "\\\\\\"})
  void los_comodines_se_buscan_como_texto_literal(String texto) throws Exception {
    assertThat(halla(texto)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"clinica_admin", "recepcionista", "veterinario"})
  void el_personal_puede_buscar(String rol) throws Exception {
    como(rol, clinicaA, get(RUTA).param("q", "jose"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.contenido[0].propietario.nombre").value("José Núñez"));
  }

  @Test
  void el_cliente_recibe_403_y_sin_token_401() throws Exception {
    como("cliente", clinicaA, get(RUTA).param("q", "maria"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
    mockMvc
        .perform(get(RUTA).param("q", "maria"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  /** Matriz de roles: `super_admin` no busca en los datos de una clínica (#170). */
  @Test
  void super_admin_con_token_de_clinica_recibe_403() throws Exception {
    como("super_admin", clinicaA, get(RUTA).param("q", "maria"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  /**
   * Con datos en las dos clínicas y el mismo texto, cada una encuentra solo lo suyo, también con
   * inactivos (#175).
   */
  @Test
  void con_datos_en_las_dos_clinicas_cada_una_encuentra_solo_lo_suyo() throws Exception {
    ejecuta(
        """
        INSERT INTO %s.propietario (nombre, telefono, activo) VALUES
          ('María Sur', '5511112222', true),
          ('Mario Baja Sur', '5533334444', false)
        """
            .formatted(clinicaB.esquema()));

    assertThat(
            nombres(
                como(
                    "clinica_admin",
                    clinicaB,
                    get(RUTA).param("q", "mari").param("incluirInactivos", "true"))))
        .containsExactlyInAnyOrder("María Sur", "Mario Baja Sur");
    assertThat(
            nombres(
                como(
                    "clinica_admin",
                    clinicaA,
                    get(RUTA).param("q", "mari").param("incluirInactivos", "true"))))
        .doesNotContain("María Sur", "Mario Baja Sur")
        .contains("María Pérez");
  }

  @Test
  void una_clinica_no_encuentra_nada_de_otra() throws Exception {
    como("clinica_admin", clinicaB, get(RUTA).param("q", "maria").param("incluirInactivos", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(0))
        .andExpect(jsonPath("$.data.contenido").isEmpty());
  }

  @Test
  void los_inactivos_solo_aparecen_cuando_el_administrador_los_pide() throws Exception {
    assertThat(halla("mari")).containsExactly("María Pérez");
    assertThat(halla("pelusa")).isEmpty();

    assertThat(
            nombres(
                como(
                    "clinica_admin",
                    clinicaA,
                    get(RUTA).param("q", "mari").param("incluirInactivos", "true"))))
        .containsExactly("María Pérez", "Mariana Baja");
    assertThat(
            nombres(
                como(
                    "clinica_admin",
                    clinicaA,
                    get(RUTA).param("q", "pelusa").param("incluirInactivos", "true"))))
        .containsExactly("Pelusa");
  }

  @ParameterizedTest
  @ValueSource(strings = {"recepcionista", "veterinario"})
  void incluir_inactivos_se_niega_a_quien_no_administra(String rol) throws Exception {
    como(rol, clinicaA, get(RUTA).param("q", "mari").param("incluirInactivos", "true"))
        .andExpect(status().isForbidden());
    como(rol, clinicaA, get(RUTA).param("q", "mari").param("incluirInactivos", "false"))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "ab", "  a  ", "   "})
  void menos_de_tres_caracteres_responde_422(String texto) throws Exception {
    como("recepcionista", clinicaA, get(RUTA).param("q", texto))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Escribe al menos 3 caracteres"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "?q=maria&page=-1",
        "?q=maria&size=0",
        "?q=maria&size=101",
        "?q=maria&page=abc",
        "?q=maria&incluirInactivos=quizas"
      })
  void una_consulta_invalida_responde_422_sin_detalles_tecnicos(String consulta) throws Exception {
    como("clinica_admin", clinicaA, get(RUTA + consulta))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("java"))));
  }

  @Test
  void pagina_primero_los_propietarios_y_despues_las_mascotas() throws Exception {
    ejecuta(
        """
        INSERT INTO %1$s.propietario (nombre, telefono) VALUES
          ('Rocío Uno', '5511110001'), ('Rocío Dos', '5511110002');
        INSERT INTO %1$s.mascota (propietario_id, nombre, especie) VALUES
          (1, 'Rocinante', 'OTRA'), (1, 'Rocco', 'PERRO')
        """
            .formatted(clinicaA.esquema()));

    assertThat(nombres(como("recepcionista", clinicaA, get(RUTA + "?q=roc&size=3"))))
        .containsExactly("Rocío Dos", "Rocío Uno", "Rocco");
    assertThat(nombres(como("recepcionista", clinicaA, get(RUTA + "?q=roc&size=3&page=1"))))
        .containsExactly("Rocinante");
    como("recepcionista", clinicaA, get(RUTA + "?q=roc&size=3&page=1"))
        .andExpect(jsonPath("$.data.total").value(4))
        .andExpect(jsonPath("$.data.pagina").value(1))
        .andExpect(jsonPath("$.data.tamano").value(3));
    como("recepcionista", clinicaA, get(RUTA + "?q=roc"))
        .andExpect(jsonPath("$.data.tamano").value(20))
        .andExpect(jsonPath("$.data.pagina").value(0));
  }

  @Test
  void con_5000_propietarios_responde_en_menos_de_un_segundo() throws Exception {
    ejecuta(
        """
        INSERT INTO %s.propietario (nombre, telefono)
        SELECT 'Persona ' || n, '55' || lpad(n::text, 8, '0') FROM generate_series(1, 5000) n
        """
            .formatted(clinicaA.esquema()));
    ejecuta(
        "INSERT INTO %s.propietario (nombre, telefono) VALUES ('Ángela Íñiguez', '5599990000')"
            .formatted(clinicaA.esquema()));
    halla("calentamiento");

    long inicio = System.nanoTime();
    List<String> hallados = halla("angela iniguez");
    Duration tardanza = Duration.ofNanos(System.nanoTime() - inicio);

    assertThat(hallados).containsExactly("Ángela Íñiguez");
    System.out.println(
        "SC-004: búsqueda entre 5000 propietarios en " + tardanza.toMillis() + " ms");
    assertThat(tardanza).as("meta SC-004").isLessThan(Duration.ofSeconds(1));
  }
}
