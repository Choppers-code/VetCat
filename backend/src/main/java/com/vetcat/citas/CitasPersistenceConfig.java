package com.vetcat.citas;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
    basePackages = "com.vetcat.citas.repository",
    entityManagerFactoryRef = "tenantEntityManagerFactory",
    transactionManagerRef = "tenantTransactionManager",
    bootstrapMode = BootstrapMode.LAZY)
public class CitasPersistenceConfig {}
