package com.rymcu.mortise.member.api.arch;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class MemberApiArchitectureTest extends AbstractLayeredArchitectureTest {

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
    void member_auth_and_family_controllers_do_not_bypass_api_facade() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.member.api.controller.MemberAuthController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.member.api.controller.FamilyController")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.member.api.service.ApiMemberService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.member.api.service.VerificationCodeService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.member.api.service.MemberContextService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.member.service.FamilyService")
                .check(importedClasses());
    }

    @Test
    void api_facade_does_not_depend_on_controller() {
        noClasses()
                .that().resideInAnyPackage("..member.api.facade..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..member.api.controller..")
                .check(importedClasses());
    }
}
