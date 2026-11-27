package com.vetcat.configuracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.catalogo.entity.Clinica;
import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.configuracion.dto.MiembroResponse;
import com.vetcat.configuracion.service.AutoasignacionDeRolesException;
import com.vetcat.configuracion.service.MiembroExistenteException;
import com.vetcat.configuracion.service.MiembroNoEncontradoException;
import com.vetcat.configuracion.service.PersonalClinicaService;
import com.vetcat.configuracion.service.PersonalInvalidoException;
import com.vetcat.configuracion.service.UltimoAdministradorException;
import com.vetcat.sesion.SinAccesoALaClinicaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Personal de la clínica con varios roles contra PostgreSQL real y dos clínicas (CON-002 a 004).
 */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba,com.vetcat.citas.entity",
      "vetcat.token.clave=clave-de-prueba-con-mas-de-32-bytes-para-hmac"
    })
class PersonalClinicaTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  @DynamicPropertySource
  static void conexion(DynamicPropertyRegistry registro) {
    registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registro.add("spring.datasource.username", POSTGRES::getUsername);
    registro.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void preparaElCatalogo() throws SQLException {
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
  }

  @Autowired PersonalClinicaService personal;
  @Autowired ClinicaRepository clinicas;
  @Autowired UsuarioClinicaRepository membresias;

  private long nuevaClinica() {
    return clinicas.save(new Clinica("Clínica", "UTC")).getId();
  }

  private static final Set<String> ADMIN = Set.of("clinica_admin");

  /** Quien hace el cambio cuando no es la propia persona afectada. */
  private static final UUID ACTOR = UUID.randomUUID();

  @Test
  void un_usuario_con_varios_roles_conserva_la_union() {
    long clinica = nuevaClinica();
    UUID ana = UUID.randomUUID();

    MiembroResponse miembro =
        personal.agregar(clinica, ACTOR, ana, Set.of("clinica_admin", "veterinario"));

    assertThat(miembro.roles()).containsExactly("clinica_admin", "veterinario");
    assertThat(membresias.findByKeycloakIdAndClinicaIdAndEstado(ana, clinica, "ACTIVO"))
        .extracting(UsuarioClinica::getRol)
        .containsExactlyInAnyOrder("clinica_admin", "veterinario");
  }

  @Test
  void rechaza_roles_que_el_administrador_no_puede_asignar() {
    long clinica = nuevaClinica();

    assertThatThrownBy(
            () -> personal.agregar(clinica, ACTOR, UUID.randomUUID(), Set.of("super_admin")))
        .isInstanceOf(PersonalInvalidoException.class);
    assertThatThrownBy(() -> personal.agregar(clinica, ACTOR, UUID.randomUUID(), Set.of("cliente")))
        .isInstanceOf(PersonalInvalidoException.class);
    assertThatThrownBy(() -> personal.agregar(clinica, ACTOR, UUID.randomUUID(), Set.of()))
        .isInstanceOf(PersonalInvalidoException.class);
  }

  @Test
  void un_administrador_no_ve_ni_modifica_el_personal_de_otra_clinica() {
    long a = nuevaClinica();
    long b = nuevaClinica();
    UUID enB = UUID.randomUUID();
    personal.agregar(b, ACTOR, enB, Set.of("veterinario"));

    assertThat(personal.listar(a)).isEmpty();
    assertThatThrownBy(() -> personal.cambiarRoles(a, ACTOR, enB, Set.of("recepcionista")))
        .isInstanceOf(MiembroNoEncontradoException.class);
    assertThatThrownBy(() -> personal.desactivar(a, enB))
        .isInstanceOf(MiembroNoEncontradoException.class);
    assertThat(personal.listar(b))
        .singleElement()
        .extracting(MiembroResponse::estado)
        .isEqualTo("ACTIVO");
  }

  @Test
  void no_deja_a_la_clinica_sin_su_ultimo_administrador() {
    long clinica = nuevaClinica();
    UUID unico = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, unico, Set.of("clinica_admin"));

    assertThatThrownBy(() -> personal.cambiarRoles(clinica, ACTOR, unico, Set.of("veterinario")))
        .isInstanceOf(UltimoAdministradorException.class);
    assertThatThrownBy(() -> personal.desactivar(clinica, unico))
        .isInstanceOf(UltimoAdministradorException.class);

    assertThat(personal.listar(clinica))
        .singleElement()
        .satisfies(m -> assertThat(m.roles()).containsExactly("clinica_admin"));
  }

  @Test
  void con_otro_administrador_activo_si_puede_quitarse_el_rol() {
    long clinica = nuevaClinica();
    UUID uno = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, uno, Set.of("clinica_admin", "veterinario"));
    personal.agregar(clinica, ACTOR, UUID.randomUUID(), ADMIN);

    MiembroResponse cambiado = personal.cambiarRoles(clinica, uno, uno, Set.of("veterinario"));

    assertThat(cambiado.roles()).containsExactly("veterinario");
  }

  @Test
  void desactivar_conserva_al_miembro_y_le_impide_abrir_sesion() {
    long clinica = nuevaClinica();
    personal.agregar(clinica, ACTOR, UUID.randomUUID(), ADMIN);
    UUID vet = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, vet, Set.of("veterinario", "recepcionista"));

    MiembroResponse inactivo = personal.desactivar(clinica, vet);

    assertThat(inactivo.estado()).isEqualTo("INACTIVO");
    assertThat(inactivo.roles()).containsExactly("recepcionista", "veterinario");
    assertThat(membresias.findByKeycloakIdAndClinicaIdAndEstado(vet, clinica, "ACTIVO")).isEmpty();

    assertThat(personal.reactivar(clinica, vet).estado()).isEqualTo("ACTIVO");
    assertThat(membresias.findByKeycloakIdAndClinicaIdAndEstado(vet, clinica, "ACTIVO")).hasSize(2);
  }

  @Test
  void cambiar_los_roles_de_un_desactivado_no_lo_reactiva() {
    long clinica = nuevaClinica();
    personal.agregar(clinica, ACTOR, UUID.randomUUID(), ADMIN);
    UUID vet = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, vet, Set.of("veterinario"));
    personal.desactivar(clinica, vet);

    MiembroResponse cambiado = personal.cambiarRoles(clinica, ACTOR, vet, Set.of("recepcionista"));

    assertThat(cambiado.estado()).isEqualTo("INACTIVO");
    assertThat(membresias.findByKeycloakIdAndClinicaIdAndEstado(vet, clinica, "ACTIVO")).isEmpty();
  }

  @Test
  void un_administrador_no_puede_anadirse_roles_a_si_mismo() {
    long clinica = nuevaClinica();
    UUID admin = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, admin, ADMIN);
    Set<String> conVeterinario = Set.of("clinica_admin", "veterinario");

    assertThatThrownBy(() -> personal.cambiarRoles(clinica, admin, admin, conVeterinario))
        .isInstanceOf(AutoasignacionDeRolesException.class);
    assertThatThrownBy(() -> personal.agregar(clinica, admin, admin, conVeterinario))
        .isInstanceOf(AutoasignacionDeRolesException.class);

    assertThat(personal.listar(clinica))
        .singleElement()
        .satisfies(m -> assertThat(m.roles()).containsExactly("clinica_admin"));
  }

  @Test
  void un_administrador_si_puede_dar_veterinario_a_otra_persona() {
    long clinica = nuevaClinica();
    UUID admin = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, admin, ADMIN);
    UUID otra = UUID.randomUUID();

    personal.agregar(clinica, admin, otra, Set.of("recepcionista"));
    MiembroResponse cambiada = personal.cambiarRoles(clinica, admin, otra, Set.of("veterinario"));

    assertThat(cambiada.roles()).containsExactly("veterinario");
  }

  @Test
  void un_rol_desactivado_no_cuenta_para_anadirselo_a_si_mismo() {
    long clinica = nuevaClinica();
    UUID persona = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, UUID.randomUUID(), ADMIN);
    personal.agregar(clinica, ACTOR, persona, Set.of("veterinario"));
    personal.desactivar(clinica, persona);

    assertThatThrownBy(
            () -> personal.cambiarRoles(clinica, persona, persona, Set.of("veterinario")))
        .isInstanceOf(AutoasignacionDeRolesException.class);
  }

  @Test
  void agregar_a_quien_ya_es_del_personal_da_conflicto_y_no_cambia_sus_roles() {
    long clinica = nuevaClinica();
    UUID vet = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, vet, Set.of("veterinario"));

    assertThatThrownBy(() -> personal.agregar(clinica, ACTOR, vet, Set.of("recepcionista")))
        .isInstanceOf(MiembroExistenteException.class);

    assertThat(personal.listar(clinica))
        .singleElement()
        .satisfies(m -> assertThat(m.roles()).containsExactly("veterinario"));
  }

  @Test
  void una_clinica_que_no_existe_se_rechaza_como_falta_de_acceso() {
    assertThatThrownBy(
            () -> personal.agregar(999_999L, ACTOR, UUID.randomUUID(), Set.of("veterinario")))
        .isInstanceOf(SinAccesoALaClinicaException.class)
        .hasMessage("No tienes acceso a esta clínica");
  }

  @Test
  void no_toca_la_fila_cliente_de_la_misma_persona() {
    long clinica = nuevaClinica();
    personal.agregar(clinica, ACTOR, UUID.randomUUID(), ADMIN);
    UUID persona = UUID.randomUUID();
    membresias.save(new UsuarioClinica(persona, clinica, "cliente"));
    personal.agregar(clinica, ACTOR, persona, Set.of("veterinario"));

    personal.desactivar(clinica, persona);

    assertThat(membresias.findByKeycloakIdAndClinicaIdAndEstado(persona, clinica, "ACTIVO"))
        .extracting(UsuarioClinica::getRol)
        .containsExactly("cliente");
  }

  @Test
  void dos_administradores_que_se_quitan_el_rol_a_la_vez_no_dejan_la_clinica_sin_ninguno()
      throws Exception {
    long clinica = nuevaClinica();
    UUID uno = UUID.randomUUID();
    UUID dos = UUID.randomUUID();
    personal.agregar(clinica, ACTOR, uno, Set.of("clinica_admin", "veterinario"));
    personal.agregar(clinica, ACTOR, dos, Set.of("clinica_admin", "veterinario"));
    CountDownLatch salida = new CountDownLatch(1);
    ExecutorService ejecutor = Executors.newFixedThreadPool(2);
    List<Future<Boolean>> tareas =
        List.of(uno, dos).stream()
            .map(
                id ->
                    ejecutor.submit(
                        () -> {
                          salida.await();
                          try {
                            personal.cambiarRoles(clinica, id, id, Set.of("veterinario"));
                            return true;
                          } catch (UltimoAdministradorException e) {
                            return false;
                          }
                        }))
            .toList();
    salida.countDown();
    int exitos = 0;
    for (Future<Boolean> tarea : tareas) {
      if (tarea.get(30, TimeUnit.SECONDS)) {
        exitos++;
      }
    }
    ejecutor.shutdown();

    assertThat(exitos).isEqualTo(1);
    assertThat(membresias.countByClinicaIdAndRolAndEstado(clinica, "clinica_admin", "ACTIVO"))
        .isEqualTo(1);
  }
}
