package com.vetcat.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vetcat.plataforma.dto.AltaClinicaRequest;
import com.vetcat.plataforma.service.AprovisionamientoClinicaService;
import com.vetcat.plataforma.service.RegistroAdministrador;
import com.vetcat.tenancy.prueba.MascotaPrueba;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Prueba que cierra la fase 2: dos clínicas reales, dadas de alta por el servicio de
 * aprovisionamiento y operadas por la unidad de persistencia de tenant, sin acceso cruzado.
 */
@Testcontainers
@SpringBootTest(
    properties = {
      "vetcat.tenant.paquetes=com.vetcat.tenancy.prueba",
      "vetcat.migraciones.tenant=filesystem:src/test/resources/migraciones-tenant"
    })
class AislamientoDeClinicasTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

  /** Sustituye al puerto de Keycloak, que no existe hasta la fase 3. */
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

  @Autowired AprovisionamientoClinicaService aprovisionamiento;

  @Autowired
  @Qualifier("tenantTransactionManager")
  PlatformTransactionManager tenantTx;

  @PersistenceContext(unitName = "tenant")
  EntityManager em;

  private static AltaClinicaRequest alta(String nombre) {
    return new AltaClinicaRequest(
        nombre, "c@vetcat.dev", null, "America/Mexico_City", "Ana", "ana@vetcat.dev");
  }

  /** Ejecuta una operación en una transacción de la unidad de tenant, para la clínica dada. */
  private <T> T enClinica(long clinicaId, java.util.function.Supplier<T> operacion) {
    return TenantContext.ejecutar(
        clinicaId, () -> new TransactionTemplate(tenantTx).execute(estado -> operacion.get()));
  }

  private void guarda(long clinicaId, long id, String nombre) {
    enClinica(
        clinicaId,
        () -> {
          em.persist(new MascotaPrueba(id, nombre));
          return null;
        });
  }

  private List<String> nombres(long clinicaId) {
    return enClinica(
        clinicaId,
        () ->
            em.createQuery("select m.nombre from MascotaPrueba m order by m.nombre", String.class)
                .getResultList());
  }

  @Test
  void una_clinica_no_ve_ni_modifica_los_datos_de_otra() {
    long a = aprovisionamiento.aprovisionar(alta("Clínica A")).id();
    long b = aprovisionamiento.aprovisionar(alta("Clínica B")).id();
    guarda(a, 1L, "paciente-de-A");
    guarda(b, 1L, "paciente-de-B");
    guarda(b, 2L, "otro-paciente-de-B");

    // Cada clínica ve solo lo suyo, aunque comparta el mismo id con la otra.
    assertThat(nombres(a)).containsExactly("paciente-de-A");
    assertThat(nombres(b)).containsExactly("otro-paciente-de-B", "paciente-de-B");

    // Acceso cruzado denegado: desde A no se encuentra, actualiza ni borra una fila de B.
    assertThat(enClinica(a, () -> em.find(MascotaPrueba.class, 2L))).isNull();
    int actualizadas =
        enClinica(
            a,
            () ->
                em.createQuery("update MascotaPrueba m set m.nombre = 'robado' where m.id = 2")
                    .executeUpdate());
    int borradas =
        enClinica(
            a, () -> em.createQuery("delete from MascotaPrueba m where m.id = 2").executeUpdate());
    assertThat(actualizadas).isZero();
    assertThat(borradas).isZero();
    assertThat(nombres(b)).containsExactly("otro-paciente-de-B", "paciente-de-B");

    // Lo que cambia o borra A tampoco toca a B.
    enClinica(
        a, () -> em.createQuery("delete from MascotaPrueba m where m.id = 1").executeUpdate());
    assertThat(nombres(a)).isEmpty();
    assertThat(nombres(b)).hasSize(2);
  }

  @Test
  void sin_clinica_activa_no_se_lee_ni_se_escribe() {
    assertThatThrownBy(
            () ->
                new TransactionTemplate(tenantTx)
                    .execute(estado -> em.find(MascotaPrueba.class, 1L)))
        .hasRootCauseInstanceOf(TenantAusenteException.class);
    assertThatThrownBy(
            () ->
                new TransactionTemplate(tenantTx)
                    .execute(
                        estado -> {
                          em.persist(new MascotaPrueba(99L, "huérfana"));
                          return null;
                        }))
        .hasRootCauseInstanceOf(TenantAusenteException.class);
  }

  @Test
  void una_clinica_inexistente_no_resuelve_datos() {
    assertThatThrownBy(() -> nombres(999_999L)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void bajo_concurrencia_cada_clinica_conserva_solo_sus_escrituras() throws Exception {
    long a = aprovisionamiento.aprovisionar(alta("Concurrente A")).id();
    long b = aprovisionamiento.aprovisionar(alta("Concurrente B")).id();
    int hilos = 6;
    int vueltas = 15;
    CountDownLatch salida = new CountDownLatch(1);
    ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
    List<Future<?>> tareas = new ArrayList<>();
    for (int h = 0; h < hilos; h++) {
      final int hilo = h;
      tareas.add(
          ejecutor.submit(
              () -> {
                salida.await();
                for (int i = 0; i < vueltas; i++) {
                  long clinica = (hilo + i) % 2 == 0 ? a : b;
                  long id = hilo * 1000L + i;
                  guarda(clinica, id, "clinica-" + clinica + "-" + id);
                }
                return null;
              }));
    }
    salida.countDown();
    for (Future<?> tarea : tareas) {
      tarea.get(60, TimeUnit.SECONDS);
    }
    ejecutor.shutdown();

    List<String> deA = nombres(a);
    List<String> deB = nombres(b);
    assertThat(deA).allMatch(n -> n.startsWith("clinica-" + a + "-"));
    assertThat(deB).allMatch(n -> n.startsWith("clinica-" + b + "-"));
    assertThat(deA.size() + deB.size()).isEqualTo(hilos * vueltas);
  }
}
