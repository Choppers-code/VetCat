package com.vetcat.citas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.vetcat.citas.entity.BloqueoAgenda;
import com.vetcat.citas.entity.Cita;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.entity.HorarioConsultorio;
import com.vetcat.security.TokenTenantService;
import com.vetcat.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {"vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"})
class EsquemaAgendaTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  private static String esquema;
  private static String otroEsquema;
  private static long clinicaId;
  private static long otraClinicaId;
  private static long veterinarioId;
  private static long otroVeterinarioId;
  private static long veterinarioInactivoId;
  private static long veterinarioOtraClinicaId;
  private static final UUID VETERINARIO_UUID =
      UUID.fromString("00000000-0000-0000-0000-000000000101");
  private static final UUID OTRO_VETERINARIO_UUID =
      UUID.fromString("00000000-0000-0000-0000-000000000102");

  @PersistenceContext(unitName = "tenant")
  EntityManager tenantEm;

  @Autowired
  @Qualifier("tenantTransactionManager")
  PlatformTransactionManager tenantTx;

  @Autowired MockMvc mockMvc;
  @Autowired TokenTenantService tokens;

  @DynamicPropertySource
  static void registraConexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void migraCatalogoYTenant() throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement()) {
      sentencia.execute("CREATE SCHEMA catalogo");
    }

    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("catalogo")
        .defaultSchema("catalogo")
        .locations("filesystem:../database/migrations/catalogo")
        .load()
        .migrate();

    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado =
            sentencia.executeQuery(
                "INSERT INTO catalogo.clinica (nombre, zona_horaria) "
                    + "VALUES ('Agenda', 'America/Mexico_City') RETURNING id, esquema")) {
      resultado.next();
      clinicaId = resultado.getLong(1);
      esquema = resultado.getString(2);
    }

    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas(esquema, "ext")
        .defaultSchema(esquema)
        .locations("filesystem:../database/migrations/tenant")
        .load()
        .migrate();

    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado =
            sentencia.executeQuery(
                "INSERT INTO catalogo.clinica (nombre, zona_horaria) "
                    + "VALUES ('Otra agenda', 'UTC') RETURNING id, esquema")) {
      resultado.next();
      otraClinicaId = resultado.getLong(1);
      otroEsquema = resultado.getString(2);
    }
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas(otroEsquema, "ext")
        .defaultSchema(otroEsquema)
        .locations("filesystem:../database/migrations/tenant")
        .load()
        .migrate();

    veterinarioId = registraVeterinario(VETERINARIO_UUID, clinicaId, "ACTIVO");
    otroVeterinarioId = registraVeterinario(OTRO_VETERINARIO_UUID, clinicaId, "ACTIVO");
    veterinarioInactivoId =
        registraVeterinario(
            UUID.fromString("00000000-0000-0000-0000-000000000103"), clinicaId, "INACTIVO");
    veterinarioOtraClinicaId =
        registraVeterinario(
            UUID.fromString("00000000-0000-0000-0000-000000000104"), otraClinicaId, "ACTIVO");
  }

  @BeforeEach
  void limpiaDatos() throws SQLException {
    ejecuta(
        "TRUNCATE "
            + esquema
            + ".horario_consultorio, "
            + esquema
            + ".bloqueo_agenda, "
            + esquema
            + ".cita, "
            + esquema
            + ".consultorio, "
            + esquema
            + ".mascota, "
            + esquema
            + ".propietario RESTART IDENTITY CASCADE");
    ejecuta(
        "TRUNCATE "
            + otroEsquema
            + ".horario_consultorio, "
            + otroEsquema
            + ".bloqueo_agenda, "
            + otroEsquema
            + ".cita, "
            + otroEsquema
            + ".consultorio, "
            + otroEsquema
            + ".mascota, "
            + otroEsquema
            + ".propietario RESTART IDENTITY CASCADE");
  }

  @Test
  void el_consultorio_rechaza_empalmes_y_las_canceladas_liberan_la_franja() throws SQLException {
    registraMascota();
    long consultorio = creaConsultorio("Consultorio 1");

    insertaCita(1, 101, consultorio, "PENDIENTE", "10:00", "11:00");

    assertThatThrownBy(() -> insertaCita(1, 102, consultorio, "PENDIENTE", "10:30", "11:30"))
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("cita_sin_empalmes_consultorio");

    insertaCita(
        1, 103, consultorio, "CANCELADA", "12:00", "13:00", 103, "Cancelada por la clínica");
    insertaCita(1, 103, consultorio, "PENDIENTE", "12:00", "13:00");

    assertThat(texto("SELECT count(*) FROM " + esquema + ".cita")).isEqualTo("3");
  }

  @Test
  void el_veterinario_no_puede_tener_empalmes_en_consultorios_distintos() throws SQLException {
    registraMascota();
    long consultorioUno = creaConsultorio("Consultorio 1");
    long consultorioDos = creaConsultorio("Consultorio 2");
    insertaCita(1, 201, consultorioUno, "PENDIENTE", "10:00", "11:00");

    assertThatThrownBy(() -> insertaCita(1, 201, consultorioDos, "PENDIENTE", "10:30", "11:30"))
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("cita_sin_empalmes_veterinario");
  }

  @Test
  void las_entidades_jpa_se_persisten_en_el_esquema_tenant() throws SQLException {
    registraMascota();

    TenantContext.ejecutar(
        1L,
        () ->
            new TransactionTemplate(tenantTx)
                .execute(
                    estado -> {
                      var consultorio = new Consultorio("Consultorio JPA");
                      tenantEm.persist(consultorio);
                      tenantEm.flush();

                      tenantEm.persist(
                          new HorarioConsultorio(
                              consultorio.getId(),
                              (short) 1,
                              null,
                              LocalTime.of(9, 0),
                              LocalTime.of(17, 0),
                              false));
                      tenantEm.persist(
                          new HorarioConsultorio(
                              consultorio.getId(),
                              null,
                              LocalDate.of(2026, 12, 25),
                              null,
                              null,
                              true));
                      tenantEm.persist(
                          new BloqueoAgenda(
                              301L,
                              null,
                              Instant.parse("2026-12-01T10:00:00Z"),
                              Instant.parse("2026-12-01T11:00:00Z"),
                              "Capacitación"));
                      tenantEm.persist(
                          new Cita(
                              1L,
                              301L,
                              consultorio.getId(),
                              "CONSULTORIO",
                              null,
                              Instant.parse("2026-12-01T11:00:00Z"),
                              Instant.parse("2026-12-01T12:00:00Z"),
                              "Consulta",
                              "PERSONAL"));
                      tenantEm.flush();
                      return null;
                    }));

    assertThat(texto("SELECT count(*) FROM " + esquema + ".horario_consultorio")).isEqualTo("2");
    assertThat(texto("SELECT count(*) FROM " + esquema + ".bloqueo_agenda")).isEqualTo("1");
    assertThat(texto("SELECT mascota_id FROM " + esquema + ".cita")).isEqualTo("1");
  }

  @Test
  void consultorios_horarios_estado_cierres_y_conflictos() throws Exception {
    String token = tokens.emitir("admin", clinicaId, List.of("clinica_admin"));
    String respuesta =
        mockMvc
            .perform(
                post("/api/v1/consultorios")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nombre\":\"Consulta\",\"activo\":true}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.activo").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long consultorioId = ((Number) JsonPath.read(respuesta, "$.data.id")).longValue();

    mockMvc
        .perform(
            post("/api/v1/horarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"diaSemana\":1,\"esCierre\":false,"
                        + "\"horaInicio\":\"09:00:00\",\"horaFin\":\"11:00:00\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.activo").value(true));

    mockMvc
        .perform(
            get("/api/v1/horarios")
                .param("consultorioId", Long.toString(consultorioId))
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));

    mockMvc
        .perform(
            post("/api/v1/horarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"diaSemana\":1,\"esCierre\":false,"
                        + "\"horaInicio\":\"10:00:00\",\"horaFin\":\"12:00:00\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc
        .perform(
            post("/api/v1/horarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"diaSemana\":1,\"esCierre\":false,"
                        + "\"horaInicio\":\"12:00:00\",\"horaFin\":\"11:00:00\"}"))
        .andExpect(status().isUnprocessableEntity());

    long horarioId = Long.parseLong(texto("SELECT id FROM " + esquema + ".horario_consultorio"));
    String marcaAnterior =
        texto(
            "SELECT actualizado_en::text FROM "
                + esquema
                + ".horario_consultorio WHERE id = "
                + horarioId);
    Thread.sleep(5);
    mockMvc
        .perform(
            put("/api/v1/horarios/" + horarioId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"diaSemana\":1,\"esCierre\":false,\"activo\":false,"
                        + "\"horaInicio\":\"09:00:00\",\"horaFin\":\"11:00:00\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.activo").value(false));
    assertThat(texto("SELECT actualizado_en::text FROM " + esquema + ".horario_consultorio"))
        .isNotEqualTo(marcaAnterior);

    mockMvc
        .perform(
            post("/api/v1/horarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"diaSemana\":1,\"esCierre\":false,"
                        + "\"horaInicio\":\"10:00:00\",\"horaFin\":\"12:00:00\"}"))
        .andExpect(status().isCreated());

    String cierre =
        mockMvc
            .perform(
                post("/api/v1/horarios")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"consultorioId\":"
                            + consultorioId
                            + ",\"fecha\":\"2026-11-30\",\"esCierre\":true}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.esCierre").value(true))
            .andExpect(jsonPath("$.data.horaInicio").value(nullValue()))
            .andExpect(jsonPath("$.data.horaFin").value(nullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long cierreId = ((Number) JsonPath.read(cierre, "$.data.id")).longValue();
    mockMvc
        .perform(
            get("/api/v1/horarios")
                .param("consultorioId", Long.toString(consultorioId))
                .param("fecha", "2026-11-30")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].esCierre").value(true));

    mockMvc
        .perform(
            post("/api/v1/horarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"fecha\":\"2026-11-30\",\"esCierre\":false,"
                        + "\"horaInicio\":\"09:00:00\",\"horaFin\":\"10:00:00\"}"))
        .andExpect(status().isConflict());

    mockMvc
        .perform(
            put("/api/v1/horarios/" + cierreId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"consultorioId\":"
                        + consultorioId
                        + ",\"fecha\":\"2026-11-30\",\"esCierre\":true,\"activo\":false}"))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            get("/api/v1/horarios")
                .param("consultorioId", Long.toString(consultorioId))
                .param("fecha", "2026-11-30")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].esCierre").value(false))
        .andExpect(jsonPath("$.data[0].horaInicio").value("10:00:00"));

    String marcaConsultorio =
        texto(
            "SELECT actualizado_en::text FROM "
                + esquema
                + ".consultorio WHERE id = "
                + consultorioId);
    Thread.sleep(5);
    mockMvc
        .perform(
            put("/api/v1/consultorios/" + consultorioId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Consulta\",\"activo\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.activo").value(false));
    assertThat(
            texto(
                "SELECT actualizado_en::text FROM "
                    + esquema
                    + ".consultorio WHERE id = "
                    + consultorioId))
        .isNotEqualTo(marcaConsultorio);

    mockMvc
        .perform(
            get("/api/v1/consultorios")
                .header(
                    "Authorization",
                    "Bearer " + tokens.emitir("otro", otraClinicaId, List.of("recepcionista"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void consultorios_exigen_autenticacion_y_rol_de_clinica() throws Exception {
    mockMvc
        .perform(get("/api/v1/consultorios"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc
        .perform(
            get("/api/v1/consultorios")
                .header(
                    "Authorization",
                    "Bearer " + tokens.emitir("cliente", clinicaId, List.of("cliente"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc
        .perform(
            post("/api/v1/consultorios")
                .header(
                    "Authorization",
                    "Bearer " + tokens.emitir("veterinario", clinicaId, List.of("veterinario")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Sin permiso\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void crear_cita_valida_devuelve_201_y_sobre() throws Exception {
    EscenarioCita escenario = escenarioCita();

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + tokenAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.mascotaId").value(1))
        .andExpect(jsonPath("$.data.veterinarioId").value(veterinarioId))
        .andExpect(jsonPath("$.data.estado").value("PENDIENTE"));
  }

  @Test
  void valida_horario_cierre_y_bloqueos_de_consultorio_y_veterinario() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String token = tokenAdmin();

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(20, 0))))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value(containsString("fuera del horario")));

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(16, 45))))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value(containsString("fuera del horario")));

    ZonedDateTime horaUtc = escenario.fecha().atTime(10, 0).atZone(ZoneId.of("UTC"));
    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitudCita(1, veterinarioId, escenario.consultorioId(), horaUtc)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value(containsString("fuera del horario")));

    ejecuta(
        "INSERT INTO "
            + esquema
            + ".horario_consultorio (consultorio_id, fecha_excepcion, es_cierre) VALUES ("
            + escenario.consultorioId()
            + ", '"
            + escenario.fecha()
            + "', true)");
    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value(containsString("cerrado")));

    ejecuta(
        "DELETE FROM "
            + esquema
            + ".horario_consultorio WHERE consultorio_id = "
            + escenario.consultorioId()
            + " AND fecha_excepcion = '"
            + escenario.fecha()
            + "'");
    insertaBloqueo(
        veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0), "Mantenimiento");
    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value(containsString("Mantenimiento")));

    insertaBloqueo(veterinarioId, null, escenario.inicio(11, 0), "Capacitación");
    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(11, 0))))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value(containsString("Capacitación")));
  }

  @Test
  void veterinario_solo_crea_citas_para_si_mismo() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String token = tokens.emitir(VETERINARIO_UUID.toString(), clinicaId, List.of("veterinario"));

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, otroVeterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value(containsString("propia agenda")))
        .andExpect(jsonPath("$.data").value(nullValue()));

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isCreated());
  }

  @Test
  void clinica_admin_y_veterinario_pueden_asignar_a_otro_veterinario() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String token =
        tokens.emitir(
            VETERINARIO_UUID.toString(), clinicaId, List.of("clinica_admin", "veterinario"));

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, otroVeterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.veterinarioId").value(otroVeterinarioId));
  }

  @Test
  void rechaza_veterinario_inactivo_y_datos_de_otra_clinica() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String token = tokenAdmin();

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1,
                        veterinarioInactivoId,
                        escenario.consultorioId(),
                        escenario.inicio(10, 0))))
        .andExpect(status().isNotFound());

    ejecuta(
        "UPDATE "
            + esquema
            + ".consultorio SET activo = false WHERE id = "
            + escenario.consultorioId());
    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isUnprocessableEntity());

    ejecuta(
        "INSERT INTO "
            + otroEsquema
            + ".propietario (nombre, telefono) VALUES ('Clínica B', '555002')");
    ejecuta(
        "INSERT INTO "
            + otroEsquema
            + ".mascota (propietario_id, nombre, especie) VALUES (1, 'Paciente B1', 'GATO')");
    ejecuta(
        "INSERT INTO "
            + otroEsquema
            + ".mascota (propietario_id, nombre, especie) VALUES (1, 'Paciente B2', 'GATO')");
    ejecuta("INSERT INTO " + otroEsquema + ".consultorio (nombre) VALUES ('Consultorio B1')");
    ejecuta("INSERT INTO " + otroEsquema + ".consultorio (nombre) VALUES ('Consultorio B2')");
    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        2, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0))))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    solicitudCita(
                        1,
                        veterinarioOtraClinicaId,
                        escenario.consultorioId(),
                        escenario.inicio(10, 0))))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/api/v1/citas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitudCita(1, veterinarioId, 2, escenario.inicio(10, 0))))
        .andExpect(status().isNotFound());
  }

  @Test
  void solicitudes_concurrentes_de_la_misma_franja_dejan_una_cita() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String token = tokenAdmin();
    CountDownLatch preparadas = new CountDownLatch(2);
    CountDownLatch iniciar = new CountDownLatch(1);
    ExecutorService ejecutores = Executors.newFixedThreadPool(2);
    try {
      Callable<MvcResult> peticion =
          () -> {
            preparadas.countDown();
            if (!iniciar.await(10, TimeUnit.SECONDS)) {
              throw new IllegalStateException("No se liberó la barrera de concurrencia");
            }
            return mockMvc
                .perform(
                    post("/api/v1/citas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            solicitudCita(
                                1,
                                veterinarioId,
                                escenario.consultorioId(),
                                escenario.inicio(10, 0))))
                .andReturn();
          };
      Future<MvcResult> primera = ejecutores.submit(peticion);
      Future<MvcResult> segunda = ejecutores.submit(peticion);
      assertThat(preparadas.await(10, TimeUnit.SECONDS)).isTrue();
      iniciar.countDown();

      MvcResult resultadoUno = primera.get(30, TimeUnit.SECONDS);
      MvcResult resultadoDos = segunda.get(30, TimeUnit.SECONDS);
      List<MvcResult> resultados = List.of(resultadoUno, resultadoDos);
      assertThat(resultados.stream().map(resultado -> resultado.getResponse().getStatus()).toList())
          .containsExactlyInAnyOrder(201, 409);
      String error =
          resultados.stream()
              .filter(resultado -> resultado.getResponse().getStatus() == 409)
              .findFirst()
              .orElseThrow()
              .getResponse()
              .getContentAsString();
      assertThat((Boolean) JsonPath.read(error, "$.success")).isFalse();
      assertThat((String) JsonPath.read(error, "$.message")).contains("franja");
      assertThat((Object) JsonPath.read(error, "$.data")).isNull();
      assertThat(texto("SELECT count(*) FROM " + esquema + ".cita")).isEqualTo("1");
    } finally {
      ejecutores.shutdownNow();
    }
  }

  /**
   * AGE-003 (#199): varias reservas a la vez sobre la misma franja y consultorio. Exactamente una
   * gana; las demás reciben 409. Seis hilos: cada petición usa una conexión de clínica y otra del
   * catálogo, y el pool de prueba es de diez.
   */
  @Test
  void seis_reservas_simultaneas_de_la_misma_franja_dejan_una_sola_cita() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String cuerpo =
        solicitudCita(1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0));

    for (int ronda = 1; ronda <= RONDAS; ronda++) {
      ejecuta("TRUNCATE " + esquema + ".cita RESTART IDENTITY");
      List<Integer> estados = reservaEnParalelo(java.util.Collections.nCopies(6, cuerpo));

      dejaConstancia("misma franja y consultorio, ronda " + ronda, estados);
      assertThat(estados).as("ronda %d", ronda).filteredOn(estado -> estado == 201).hasSize(1);
      assertThat(estados).as("ronda %d", ronda).filteredOn(estado -> estado == 409).hasSize(5);
      assertThat(texto("SELECT count(*) FROM " + esquema + ".cita")).isEqualTo("1");
    }
  }

  /** Variante de #199: consultorios y veterinarios distintos a la misma hora; las dos ganan. */
  @Test
  void en_consultorios_y_veterinarios_distintos_las_dos_reservas_ganan() throws Exception {
    EscenarioCita escenario = escenarioCita();
    long otroConsultorio = consultorioConHorario("Consultorio 2", escenario.fecha());

    List<Integer> estados =
        reservaEnParalelo(
            List.of(
                solicitudCita(1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0)),
                solicitudCita(1, otroVeterinarioId, otroConsultorio, escenario.inicio(10, 0))));

    dejaConstancia("consultorios y veterinarios distintos", estados);
    assertThat(estados).containsExactly(201, 201);
    assertThat(texto("SELECT count(*) FROM " + esquema + ".cita")).isEqualTo("2");
  }

  /** El mismo veterinario no puede estar en dos consultorios a la vez, aunque lleguen juntas. */
  @Test
  void el_mismo_veterinario_en_dos_consultorios_a_la_vez_solo_gana_una() throws Exception {
    EscenarioCita escenario = escenarioCita();
    long otroConsultorio = consultorioConHorario("Consultorio 2", escenario.fecha());
    List<String> cuerpos =
        List.of(
            solicitudCita(1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0)),
            solicitudCita(1, veterinarioId, otroConsultorio, escenario.inicio(10, 0)));

    for (int ronda = 1; ronda <= RONDAS; ronda++) {
      ejecuta("TRUNCATE " + esquema + ".cita RESTART IDENTITY");
      List<Integer> estados = reservaEnParalelo(cuerpos);

      dejaConstancia("mismo veterinario, dos consultorios, ronda " + ronda, estados);
      assertThat(estados).as("ronda %d", ronda).containsExactlyInAnyOrder(201, 409);
      assertThat(texto("SELECT count(*) FROM " + esquema + ".cita")).isEqualTo("1");
    }
  }

  /**
   * Criterio de #199: sin las restricciones de exclusión, la prueba de concurrencia lo detecta. Se
   * quitan las dos {@code EXCLUDE} del esquema, se repite la carrera y deben entrar varias citas;
   * al terminar se restauran con su definición original.
   */
  @Test
  void sin_las_exclusiones_la_carrera_deja_citas_duplicadas() throws Exception {
    EscenarioCita escenario = escenarioCita();
    String cuerpo =
        solicitudCita(1, veterinarioId, escenario.consultorioId(), escenario.inicio(10, 0));
    List<String> nombres =
        List.of("cita_sin_empalmes_consultorio", "cita_sin_empalmes_veterinario");
    List<String> definiciones = new java.util.ArrayList<>();
    for (String nombre : nombres) {
      definiciones.add(
          texto(
              "SELECT pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = '"
                  + nombre
                  + "' AND connamespace = '"
                  + esquema
                  + "'::regnamespace"));
    }
    try {
      for (String nombre : nombres) {
        ejecuta("ALTER TABLE " + esquema + ".cita DROP CONSTRAINT " + nombre);
      }

      List<Integer> estados = reservaEnParalelo(java.util.Collections.nCopies(6, cuerpo));

      dejaConstancia("sin EXCLUDE (control)", estados);
      assertThat(Integer.parseInt(texto("SELECT count(*) FROM " + esquema + ".cita")))
          .as("sin las EXCLUDE entra más de una cita en la misma franja")
          .isGreaterThan(1);
    } finally {
      ejecuta("TRUNCATE " + esquema + ".cita RESTART IDENTITY");
      for (int i = 0; i < nombres.size(); i++) {
        ejecuta(
            "ALTER TABLE "
                + esquema
                + ".cita ADD CONSTRAINT "
                + nombres.get(i)
                + " "
                + definiciones.get(i));
      }
    }
  }

  /**
   * Rondas de cada carrera: una sola puede salir bien por suerte del orden de llegada. Con varias,
   * un 500 por interbloqueo aparece de forma fiable (#199).
   */
  private static final int RONDAS = 5;

  /**
   * Lanza las peticiones a la vez: todas esperan en la misma barrera y salen juntas. No usa {@code
   * sleep}: la sincronización es un {@link CountDownLatch}.
   */
  private List<Integer> reservaEnParalelo(List<String> cuerpos) throws Exception {
    String token = tokenAdmin();
    CountDownLatch preparadas = new CountDownLatch(cuerpos.size());
    CountDownLatch salida = new CountDownLatch(1);
    ExecutorService hilos = Executors.newFixedThreadPool(cuerpos.size());
    try {
      List<Future<Integer>> futuros = new java.util.ArrayList<>();
      for (String cuerpo : cuerpos) {
        futuros.add(
            hilos.submit(
                () -> {
                  preparadas.countDown();
                  if (!salida.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("No se liberó la barrera de concurrencia");
                  }
                  try {
                    return mockMvc
                        .perform(
                            post("/api/v1/citas")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                  } catch (jakarta.servlet.ServletException excepcion) {
                    // Una excepción sin manejar llega al cliente como 500: se cuenta como tal
                    // para que la constancia diga qué recibió cada petición.
                    System.out.println("[#199] petición con excepción sin manejar: " + excepcion);
                    return 500;
                  }
                }));
      }
      assertThat(preparadas.await(10, TimeUnit.SECONDS)).isTrue();
      salida.countDown();
      List<Integer> estados = new java.util.ArrayList<>();
      for (Future<Integer> futuro : futuros) {
        estados.add(futuro.get(60, TimeUnit.SECONDS));
      }
      return estados;
    } finally {
      hilos.shutdownNow();
    }
  }

  /** Deja en la salida de la prueba cuántas ganaron y cuántas recibieron 409 (#199). */
  private static void dejaConstancia(String caso, List<Integer> estados) {
    long ganadoras = estados.stream().filter(estado -> estado == 201).count();
    long conflictos = estados.stream().filter(estado -> estado == 409).count();
    System.out.printf(
        "[#199] %s: %d peticiones, %d ganaron (201), %d recibieron 409, %d otro, estados=%s%n",
        caso,
        estados.size(),
        ganadoras,
        conflictos,
        estados.size() - ganadoras - conflictos,
        estados);
  }

  private static long consultorioConHorario(String nombre, LocalDate fecha) throws SQLException {
    long id = creaConsultorio(nombre);
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".horario_consultorio (consultorio_id, dia_semana, inicio, fin) VALUES ("
            + id
            + ", "
            + fecha.getDayOfWeek().getValue()
            + ", '09:00', '17:00')");
    return id;
  }

  private EscenarioCita escenarioCita() throws SQLException {
    registraMascota();
    ZoneId zona = ZoneId.of("America/Mexico_City");
    LocalDate fecha = LocalDate.now(zona).plusDays(2);
    long consultorioId = creaConsultorio("Consultorio de citas");
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".horario_consultorio (consultorio_id, dia_semana, inicio, fin) VALUES ("
            + consultorioId
            + ", "
            + fecha.getDayOfWeek().getValue()
            + ", '09:00', '17:00')");
    return new EscenarioCita(consultorioId, fecha, zona);
  }

  private String tokenAdmin() {
    return tokens.emitir("admin", clinicaId, List.of("clinica_admin"));
  }

  private static String solicitudCita(
      long mascotaId, long veterinarioId, long consultorioId, ZonedDateTime inicio) {
    return """
        {"mascotaId":%d,"veterinarioId":%d,"consultorioId":%d,"inicio":"%s","duracionMinutos":30}
        """
        .formatted(mascotaId, veterinarioId, consultorioId, inicio.toOffsetDateTime());
  }

  private static void insertaBloqueo(
      long veterinarioId, Long consultorioId, ZonedDateTime inicio, String motivo)
      throws SQLException {
    String valores = consultorioId == null ? veterinarioId + ", NULL" : "NULL, " + consultorioId;
    Instant desde = inicio.toInstant();
    Instant hasta = inicio.plusMinutes(30).toInstant();
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".bloqueo_agenda (veterinario_id, consultorio_id, inicio, fin, motivo) VALUES ("
            + valores
            + ", '"
            + desde
            + "', '"
            + hasta
            + "', '"
            + motivo
            + "')");
  }

  private record EscenarioCita(long consultorioId, LocalDate fecha, ZoneId zona) {
    ZonedDateTime inicio(int hora, int minuto) {
      return fecha.atTime(hora, minuto).atZone(zona);
    }
  }

  private static long registraVeterinario(UUID usuarioId, long idClinica, String estado)
      throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado =
            sentencia.executeQuery(
                "INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol, estado) "
                    + "VALUES ('"
                    + usuarioId
                    + "', "
                    + idClinica
                    + ", 'veterinario', '"
                    + estado
                    + "') RETURNING id")) {
      resultado.next();
      return resultado.getLong(1);
    }
  }

  private static Connection conexion() throws SQLException {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  }

  private static void ejecuta(String sql) throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement()) {
      sentencia.execute(sql);
    }
  }

  private static String texto(String sql) throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado = sentencia.executeQuery(sql)) {
      resultado.next();
      return resultado.getString(1);
    }
  }

  private static long creaConsultorio(String nombre) throws SQLException {
    try (Connection conexion = conexion();
        Statement sentencia = conexion.createStatement();
        ResultSet resultado =
            sentencia.executeQuery(
                "INSERT INTO "
                    + esquema
                    + ".consultorio (nombre) VALUES ('"
                    + nombre
                    + "') RETURNING id")) {
      resultado.next();
      return resultado.getLong(1);
    }
  }

  private static void registraMascota() throws SQLException {
    ejecuta("INSERT INTO " + esquema + ".propietario (nombre, telefono) VALUES ('Ana', '555')");
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".mascota (propietario_id, nombre, especie) VALUES (1, 'Luna', 'GATO')");
  }

  private static void insertaCita(
      long mascotaId,
      long veterinarioId,
      long consultorioId,
      String estado,
      String inicio,
      String fin)
      throws SQLException {
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".cita (mascota_id, veterinario_id, consultorio_id, tipo, inicio, fin, estado) "
            + "VALUES ("
            + mascotaId
            + ", "
            + veterinarioId
            + ", "
            + consultorioId
            + ", 'CONSULTORIO', '2026-12-01 "
            + inicio
            + ":00+00', '2026-12-01 "
            + fin
            + ":00+00', '"
            + estado
            + "')");
  }

  private static void insertaCita(
      long mascotaId,
      long veterinarioId,
      long consultorioId,
      String estado,
      String inicio,
      String fin,
      long canceladaPor,
      String motivoCancelacion)
      throws SQLException {
    ejecuta(
        "INSERT INTO "
            + esquema
            + ".cita (mascota_id, veterinario_id, consultorio_id, tipo, inicio, fin, estado, "
            + "cancelada_por, motivo_cancelacion) VALUES ("
            + mascotaId
            + ", "
            + veterinarioId
            + ", "
            + consultorioId
            + ", 'CONSULTORIO', '2026-12-01 "
            + inicio
            + ":00+00', '2026-12-01 "
            + fin
            + ":00+00', '"
            + estado
            + "', "
            + canceladaPor
            + ", '"
            + motivoCancelacion
            + "')");
  }
}
