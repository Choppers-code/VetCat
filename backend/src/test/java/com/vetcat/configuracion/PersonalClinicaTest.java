package com.vetcat.configuracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.catalogo.entity.Clinica;
import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.configuracion.dto.MiembroResponse;
import com.vetcat.configuracion.service.MiembroNoEncontradoException;
import com.vetcat.configuracion.service.PersonalClinicaService;
import com.vetcat.configuracion.service.PersonalInvalidoException;
import com.vetcat.configuracion.service.UltimoAdministradorException;
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
@SpringBootTest(properties = "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba")
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

  @Test
  void un_usuario_con_varios_roles_conserva_la_union() {
    long clinica = nuevaClinica();
    UUID ana = UUID.randomUUID();

    MiembroResponse miembro =
        personal.agregar(clinica, ana, Set.of("clinica_admin", "veterinario"));

    assertThat(miembro.roles()).containsExactly("clinica_admin", "veterinario");
    assertThat(membresias.findByKeycloakIdAndClinicaIdAndEstado(ana, clinica, "ACTIVO"))
        .extracting(UsuarioClinica::getRol)
        .containsExactlyInAnyOrder("clinica_admin", "veterinario");
  }

  @Test
  void rechaza_roles_que_el_administrador_no_puede_asignar() {
    long clinica = nuevaClinica();

    assertThatThrownBy(() -> personal.agregar(clinica, UUID.randomUUID(), Set.of("super_admin")))
        .isInstanceOf(PersonalInvalidoException.class);
    assertThatThrownBy(() -> personal.agregar(clinica, UUID.randomUUID(), Set.of("cliente")))
        .isInstanceOf(PersonalInvalidoException.class);
    assertThatThrownBy(() -> personal.agregar(clinica, UUID.randomUUID(), Set.of()))
        .isInstanceOf(PersonalInvalidoException.class);
  }

  @Test
  void un_administrador_no_ve_ni_modifica_el_personal_de_otra_clinica() {
    long a = nuevaClinica();
    long b = nuevaClinica();
    UUID enB = UUID.randomUUID();
    personal.agregar(b, enB, Set.of("veterinario"));

    assertThat(personal.listar(a)).isEmpty();
    assertThatThrownBy(() -> personal.cambiarRoles(a, enB, Set.of("recepcionista")))
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
    personal.agregar(clinica, unico, Set.of("clinica_admin"));

    assertThatThrownBy(() -> personal.cambiarRoles(clinica, unico, Set.of("veterinario")))
        .isInstanceOf(UltimoAdministradorException.class);

    assertThat(personal.listar(clinica))
        .singleElement()
        .satisfies(m -> assertThat(m.roles()).containsExactly("clinica_admin"));
  }

  @Test
  void con_otro_administrador_activo_si_puede_quitarse_el_rol() {
    long clinica = nuevaClinica();
    UUID uno = UUID.randomUUID();
    personal.agregar(clinica, uno, ADMIN);
    personal.agregar(clinica, UUID.randomUUID(), ADMIN);

    MiembroResponse cambiado = personal.cambiarRoles(clinica, uno, Set.of("veterinario"));

    assertThat(cambiado.roles()).containsExactly("veterinario");
  }

  @Test
  void dos_administradores_que_se_quitan_el_rol_a_la_vez_no_dejan_la_clinica_sin_ninguno()
      throws Exception {
    long clinica = nuevaClinica();
    UUID uno = UUID.randomUUID();
    UUID dos = UUID.randomUUID();
    personal.agregar(clinica, uno, ADMIN);
    personal.agregar(clinica, dos, ADMIN);
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
                            personal.cambiarRoles(clinica, id, Set.of("veterinario"));
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
