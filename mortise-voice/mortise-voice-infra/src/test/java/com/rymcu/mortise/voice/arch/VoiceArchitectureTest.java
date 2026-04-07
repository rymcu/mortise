package com.rymcu.mortise.voice.arch;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class VoiceArchitectureTest extends AbstractLayeredArchitectureTest {

    private static final String BASE_PACKAGE = "com.rymcu.mortise.voice";

    @Override
    protected String basePackage() {
        return BASE_PACKAGE;
    }

    @Override
    protected JavaClasses importedClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage());
    }

    @Test
    void voice_module_respects_layered_contract() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer("AdminApi").definedBy("..voice.admin..", "..voice.api..")
                .layer("Application").definedBy("..voice.application..")
                .layer("Domain").definedBy("..voice.entity..", "..voice.repository..")
                .layer("Kernel").definedBy("..voice.kernel..")
                .layer("Infrastructure").definedBy("..voice.infra..", "..voice.mapper..")
                .whereLayer("AdminApi").mayOnlyAccessLayers("Application")
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "Kernel")
                .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Kernel")
                .check(importedClasses());
    }

    @Test
    void admin_and_api_do_not_depend_on_domain_or_infra() {
        noClasses()
                .that().resideInAnyPackage("..voice.admin..", "..voice.api..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..voice.entity..",
                        "..voice.repository..",
                        "..voice.kernel..",
                        "..voice.infra..",
                        "..voice.mapper.."
                )
                .check(importedClasses());
    }

    @Test
    void application_does_not_depend_on_infrastructure() {
        noClasses()
                .that().resideInAnyPackage("..voice.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..voice.infra..", "..voice.mapper..")
                .check(importedClasses());
    }
}