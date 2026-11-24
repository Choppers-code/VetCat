package com.vetcat.tenancy;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Unidad de persistencia de los esquemas de clínica, con multi-tenant por esquema.
 *
 * <p>Hibernate pide al {@link CurrentTenantResolver} el tenant de cada operación y al {@link
 * SchemaMultiTenantConnectionProvider} la conexión con el {@code search_path} de esa clínica. No
 * hay caché de segundo nivel: compartiría entidades entre clínicas (docs/Decisiones_tecnicas.md
 * §2).
 */
@Configuration
public class TenantPersistenceConfig {

  /** Paquetes con entidades de clínica; cada módulo añade el suyo al nacer. */
  private static final String PAQUETES_POR_DEFECTO =
      "com.vetcat.propietarios,com.vetcat.mascotas,com.vetcat.expediente,com.vetcat.vacunas,"
          + "com.vetcat.citas,com.vetcat.facturacion,com.vetcat.portal";

  @Bean
  public SchemaMultiTenantConnectionProvider schemaMultiTenantConnectionProvider(
      DataSource dataSource) {
    return new SchemaMultiTenantConnectionProvider(dataSource);
  }

  @Bean
  public CurrentTenantResolver currentTenantResolver() {
    return new CurrentTenantResolver();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
      SchemaMultiTenantConnectionProvider proveedor,
      CurrentTenantResolver resolutor,
      @Value("${vetcat.tenant.paquetes:" + PAQUETES_POR_DEFECTO + "}") String[] paquetes) {
    var fabrica = new LocalContainerEntityManagerFactoryBean();
    fabrica.setPersistenceUnitName("tenant");
    // Sin DataSource propio: las conexiones las entrega el proveedor, ya con el esquema fijado.
    fabrica.setPackagesToScan(paquetes);
    fabrica.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    fabrica.setJpaPropertyMap(
        Map.of(
            "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
            "hibernate.hbm2ddl.auto", "none",
            "hibernate.boot.allow_jdbc_metadata_access", "false",
            "hibernate.multi_tenant_connection_provider", proveedor,
            "hibernate.tenant_identifier_resolver", resolutor,
            "hibernate.cache.use_second_level_cache", "false",
            "hibernate.cache.use_query_cache", "false"));
    return fabrica;
  }

  /**
   * Sin {@code @Primary}: un {@code @Transactional} sin nombre debe fallar, no elegir una unidad.
   */
  @Bean
  public PlatformTransactionManager tenantTransactionManager(
      @Qualifier("tenantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
