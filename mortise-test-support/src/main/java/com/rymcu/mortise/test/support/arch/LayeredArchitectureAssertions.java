package com.rymcu.mortise.test.support.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Shared ArchUnit assertions for Mortise layered modules.
 */
public final class LayeredArchitectureAssertions {

    private LayeredArchitectureAssertions() {
    }

    public static ArchRule noControllerToEntityDependency(String basePackage) {
        return classes()
                .that().resideInAPackage(basePackage + "..controller..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackage(basePackage + "..entity..")
                .orShould().resideInAnyPackage(
                        "java..",
                        "jakarta..",
                        "org.springframework..",
                        "io.swagger..",
                        "lombok..",
                        basePackage + "..controller..",
                        basePackage + "..model..",
                        basePackage + "..service..",
                        basePackage + "..query..",
                        "com.rymcu.mortise.core..",
                        "com.rymcu.mortise.common..",
                        "com.rymcu.mortise.web..",
                        "com.rymcu.mortise.log.."
                );
    }

    public static ArchRule layeredSystemArchitecture(String basePackage) {
        return layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer("Controller").definedBy(basePackage + "..controller..")
                .layer("Application").definedBy(basePackage + "..service..", basePackage + "..query..")
                .layer("Domain").definedBy(basePackage + "..entity..", basePackage + "..model..")
                .layer("Infrastructure").definedBy(basePackage + "..mapper..", basePackage + "..storage..")
                .whereLayer("Controller").mayOnlyAccessLayers("Application", "Domain")
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "Infrastructure")
                .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain");
    }

    public static void assertSystemArchitecture(JavaClasses classes, String basePackage) {
        layeredSystemArchitecture(basePackage).check(classes);
        noControllerToEntityDependency(basePackage).check(classes);
    }
}
