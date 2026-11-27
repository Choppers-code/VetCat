package com.vetcat.citas;

import com.vetcat.citas.repository.BloqueoAgendaRepository;
import com.vetcat.citas.repository.CitaRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
    basePackageClasses = {BloqueoAgendaRepository.class, CitaRepository.class},
    entityManagerFactoryRef = "tenantEntityManagerFactory",
    transactionManagerRef = "tenantTransactionManager",
    bootstrapMode = BootstrapMode.LAZY)
public class CitasPersistenceConfig {}
