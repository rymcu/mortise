package com.rymcu.mortise.product.api.arch;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ProductApiArchitectureTest extends AbstractLayeredArchitectureTest {

    @Override
    protected String basePackage() {
        return "com.rymcu.mortise.product";
    }

    @Override
    protected JavaClasses importedClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage());
    }

    @Test
    void api_controllers_do_not_depend_on_mixed_services() {
        noClasses()
                .that().resideInAnyPackage("..product.api.controller..")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.product.service.ProductService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.product.service.ProductCategoryService")
                .orShould().dependOnClassesThat()
                .resideInAnyPackage("..product.mapper..")
                .check(importedClasses());
    }

    @Test
    void api_facades_do_not_depend_on_controller() {
        noClasses()
                .that().resideInAnyPackage("..product.api.facade..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..product.api.controller..")
                .check(importedClasses());
    }
}
