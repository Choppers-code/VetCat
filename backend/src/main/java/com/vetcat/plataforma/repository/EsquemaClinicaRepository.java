package com.vetcat.plataforma.repository;

import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Operaciones DDL sobre el esquema de una clínica. */
@Repository
public class EsquemaClinicaRepository {

  private static final Pattern ESQUEMA_DE_CLINICA = Pattern.compile("clinica_\\d{3,}");

  private final JdbcClient jdbc;

  public EsquemaClinicaRepository(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  /**
   * Borra el esquema de una clínica que se acaba de crear y cuyo alta falló.
   *
   * <p>Solo se usa para deshacer un alta en la misma operación que la creó: la clínica es nueva,
   * nadie tiene acceso y no hay datos de negocio que perder.
   */
  public void eliminar(String esquema) {
    if (!ESQUEMA_DE_CLINICA.matcher(esquema).matches()) {
      throw new IllegalArgumentException("Esquema de clínica inválido");
    }
    jdbc.sql("DROP SCHEMA IF EXISTS \"" + esquema + "\" CASCADE").update();
  }
}
