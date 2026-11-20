package com.vetcat;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/** Vigila que las capas no se salten: controller → service → repository → entity/dto. */
class ArquitecturaCapasTest {

  private static final JavaClasses CLASES =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("com.vetcat");

  @Test
  void las_capas_solo_dependen_hacia_abajo() {
    layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("controller")
        .definedBy("..controller..")
        .layer("service")
        .definedBy("..service..")
        .layer("repository")
        .definedBy("..repository..")
        .layer("modelo")
        .definedBy("..entity..", "..dto..")
        .whereLayer("controller")
        .mayNotBeAccessedByAnyLayer()
        .whereLayer("service")
        .mayOnlyBeAccessedByLayers("controller", "service")
        .whereLayer("repository")
        .mayOnlyBeAccessedByLayers("service")
        .allowEmptyShould(true)
        .check(CLASES);
  }
}
