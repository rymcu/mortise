package com.rymcu.mortise.product.arch;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ProductArchitectureTest extends AbstractLayeredArchitectureTest {

    private static final String[] ADMIN_CONTROLLER_PACKAGES = {
            "..product.admin.controller.."
    };
    private static final String[] ADMIN_FACADE_PACKAGES = {
            "..product.admin.facade.."
    };
    private static final String[] APPLICATION_COMMAND_PACKAGES = {
            "..product.service.command.."
    };
    private static final String[] APPLICATION_QUERY_PACKAGES = {
            "..product.service.query.."
    };

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
    void controllers_use_facade_instead_of_mixed_services_or_mapper_runtime() {
        noClasses()
                .that().resideInAnyPackage(ADMIN_CONTROLLER_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..product.service..",
                        "..product.mapper..",
                        "com.mybatisflex.spring.service.impl..",
                        "com.mybatisflex.core.service.."
                )
                .check(importedClasses());
    }

    @Test
    void facades_coordinate_command_query_services_only() {
        noClasses()
                .that().resideInAnyPackage(ADMIN_FACADE_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..product.admin.controller..",
                        "..product.mapper.."
                )
                .check(importedClasses());

        noClasses()
                .that().resideInAnyPackage(ADMIN_FACADE_PACKAGES)
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.product.service.ProductService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.product.service.ProductCategoryService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.product.service.ProductSkuService")
                .check(importedClasses());
    }

    @Test
    void query_services_do_not_depend_on_command_services() {
        noClasses()
                .that().resideInAnyPackage(APPLICATION_QUERY_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(APPLICATION_COMMAND_PACKAGES)
                .check(importedClasses());
    }

    @Test
    void command_services_do_not_depend_on_query_services() {
        noClasses()
                .that().resideInAnyPackage(APPLICATION_COMMAND_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(APPLICATION_QUERY_PACKAGES)
                .check(importedClasses());
    }
}
