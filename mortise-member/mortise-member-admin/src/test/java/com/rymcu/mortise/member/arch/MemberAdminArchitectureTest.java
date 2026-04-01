package com.rymcu.mortise.member.arch;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class MemberAdminArchitectureTest extends AbstractLayeredArchitectureTest {

    @Override
    protected String basePackage() {
        return "com.rymcu.mortise.member";
    }

    @Override
    protected JavaClasses importedClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage());
    }

    @Test
    void admin_controller_uses_facade_instead_of_admin_service_or_application_service() {
        noClasses()
                .that().resideInAnyPackage("..member.admin.controller..")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.member.admin.service.AdminMemberService")
                .orShould().dependOnClassesThat()
                .resideInAnyPackage(
                        "..member.service..",
                        "..member.mapper.."
                )
                .check(importedClasses());
    }

    @Test
    void admin_facade_does_not_reach_back_to_controller() {
        noClasses()
                .that().resideInAnyPackage("..member.admin.facade..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..member.admin.controller..")
                .check(importedClasses());
    }
}
