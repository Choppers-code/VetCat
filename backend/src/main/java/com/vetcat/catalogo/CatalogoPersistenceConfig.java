package com.vetcat.catalogo;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Unidad de persistencia del esquema global {@code catalogo}, sin multi-tenant.
 *
 * <p>Se mantiene separada de la unidad de las clínicas: mezclar entidades globales y de tenant en
 * una sola unidad es la parte más frágil de la fase (docs/Decisiones_tecnicas.md §1).
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.vetcat.catalogo.repository",
    entityManagerFactoryRef = "catalogoEntityManagerFactory",
    transactionManagerRef = "catalogoTransactionManager")
public class CatalogoPersistenceConfig {

  @Bean
  public LocalContainerEntityManagerFactoryBean catalogoEntityManagerFactory(
      DataSource dataSource) {
    var fabrica = new LocalContainerEntityManagerFactoryBean();
    fabrica.setPersistenceUnitName("catalogo");
    fabrica.setDataSource(dataSource);
    fabrica.setPackagesToScan("com.vetcat.catalogo.entity");
    fabrica.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    fabrica.setJpaPropertyMap(
        Map.of(
            "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
            // Las migraciones de Flyway mandan; Hibernate solo comprueba que las entidades encajan.
            "hibernate.hbm2ddl.auto", "validate",
            "hibernate.cache.use_second_level_cache", "false",
            "hibernate.cache.use_query_cache", "false"));
    return fabrica;
  }

  /**
   * Sin {@code @Primary}: un {@code @Transactional} sin nombre debe fallar, no elegir una unidad.
   */
  @Bean
  public PlatformTransactionManager catalogoTransactionManager(
      @Qualifier("catalogoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
