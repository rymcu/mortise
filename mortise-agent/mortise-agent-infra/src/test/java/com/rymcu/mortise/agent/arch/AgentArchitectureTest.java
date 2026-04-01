package com.rymcu.mortise.agent.arch;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class AgentArchitectureTest extends AbstractLayeredArchitectureTest {

    private static final String BASE_PACKAGE = "com.rymcu.mortise.agent";
    private static final String[] ADMIN_API_PACKAGES = {
            "..agent.admin..",
            "..agent.api.."
    };
    private static final String[] ADMIN_CONTROLLER_PACKAGES = {
            "..agent.admin.controller.."
    };
    private static final String[] ADMIN_FACADE_PACKAGES = {
            "..agent.admin.facade.."
    };
    private static final String[] ADMIN_ASSEMBLER_PACKAGES = {
            "..agent.admin.assembler.."
    };
    private static final String[] ADMIN_CONTRACT_PACKAGES = {
            "..agent.admin.contract.."
    };
    private static final String[] API_CONTROLLER_PACKAGES = {
            "..agent.api.controller.."
    };
    private static final String[] API_FACADE_PACKAGES = {
            "..agent.api.facade.."
    };
    private static final String[] API_ASSEMBLER_PACKAGES = {
            "..agent.api.assembler.."
    };
    private static final String[] API_CONTRACT_PACKAGES = {
            "..agent.api.contract.."
    };
    private static final String[] APPLICATION_PACKAGES = {
            "..agent.application.."
    };
    private static final String[] APPLICATION_COMMAND_SERVICE_PACKAGES = {
            "..agent.application.service.command.."
    };
    private static final String[] APPLICATION_QUERY_SERVICE_PACKAGES = {
            "..agent.application.service.query.."
    };
    private static final String[] LEGACY_APPLICATION_SERVICE_PACKAGES = {
            "..agent.application.service.ai..",
            "..agent.application.service.conversation.."
    };
    private static final String[] DOMAIN_PACKAGES = {
            "..agent.entity..",
            "..agent.model..",
            "..agent.repository.."
    };
    private static final String[] KERNEL_PACKAGES = {
            "..agent.kernel.."
    };
    private static final String[] INFRA_PACKAGES = {
            "..agent.infra..",
            "..agent.mapper.."
    };

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
    void agent_module_respects_layered_contract() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer("AdminApi").definedBy(ADMIN_API_PACKAGES)
                .layer("Application").definedBy(APPLICATION_PACKAGES)
                .layer("Domain").definedBy(DOMAIN_PACKAGES)
                .layer("Kernel").definedBy(KERNEL_PACKAGES)
                .layer("Infrastructure").definedBy(INFRA_PACKAGES)
                .whereLayer("AdminApi").mayOnlyAccessLayers("Application")
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "Kernel")
                .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Kernel")
                .check(importedClasses());
    }

    @Test
    void api_and_admin_do_not_bypass_application_layer() {
        noClasses()
                .that().resideInAnyPackage(ADMIN_API_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..agent.entity..",
                        "..agent.model..",
                        "..agent.repository..",
                        "..agent.kernel..",
                        "..agent.infra..",
                        "..agent.mapper.."
                )
                .check(importedClasses());
    }

    @Test
    void controllers_only_depend_on_contract_and_facade() {
        JavaClasses classes = importedClasses();

        noClasses()
                .that().resideInAnyPackage(API_CONTROLLER_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(concat(
                        API_ASSEMBLER_PACKAGES,
                        new String[] {
                        "..agent.application..",
                        "..agent.kernel..",
                        "..agent.entity..",
                        "..agent.model..",
                        "..agent.repository..",
                        "..agent.infra..",
                        "..agent.mapper.."
                        }
                ))
                .check(classes);

        noClasses()
                .that().resideInAnyPackage(ADMIN_CONTROLLER_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(concat(
                        ADMIN_ASSEMBLER_PACKAGES,
                        API_CONTROLLER_PACKAGES,
                        API_FACADE_PACKAGES,
                        API_ASSEMBLER_PACKAGES,
                        API_CONTRACT_PACKAGES,
                        new String[] {
                        "..agent.application..",
                        "..agent.kernel..",
                        "..agent.entity..",
                        "..agent.model..",
                        "..agent.repository..",
                        "..agent.infra..",
                        "..agent.mapper.."
                        }
                ))
                .check(classes);
    }

    @Test
    void admin_facade_only_coordinates_contract_assembler_and_application() {
        noClasses()
                .that().resideInAnyPackage(ADMIN_FACADE_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(concat(
                        ADMIN_CONTROLLER_PACKAGES,
                        API_CONTROLLER_PACKAGES,
                        API_FACADE_PACKAGES,
                        API_ASSEMBLER_PACKAGES,
                        API_CONTRACT_PACKAGES,
                        new String[] {
                        "..agent.entity..",
                        "..agent.model..",
                        "..agent.repository..",
                        "..agent.kernel..",
                        "..agent.infra..",
                        "..agent.mapper.."
                        }
                ))
                .check(importedClasses());
    }

    @Test
    void api_and_admin_facades_do_not_depend_on_legacy_mixed_application_services() {
        noClasses()
                .that().resideInAnyPackage(ADMIN_FACADE_PACKAGES)
                .or().resideInAnyPackage(API_FACADE_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(LEGACY_APPLICATION_SERVICE_PACKAGES)
                .check(importedClasses());
    }

    @Test
    void admin_assembler_stays_as_translation_layer() {
        noClasses()
                .that().resideInAnyPackage(ADMIN_ASSEMBLER_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(concat(
                        ADMIN_CONTROLLER_PACKAGES,
                        ADMIN_FACADE_PACKAGES,
                        API_CONTROLLER_PACKAGES,
                        API_FACADE_PACKAGES,
                        API_ASSEMBLER_PACKAGES,
                        API_CONTRACT_PACKAGES,
                        new String[] {
                        "..agent.entity..",
                        "..agent.model..",
                        "..agent.repository..",
                        "..agent.kernel..",
                        "..agent.infra..",
                        "..agent.mapper.."
                        }
                ))
                .check(importedClasses());
    }

    @Test
    void application_does_not_reach_infrastructure() {
        noClasses()
                .that().resideInAnyPackage(APPLICATION_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(INFRA_PACKAGES)
                .check(importedClasses());
    }

    @Test
    void query_services_do_not_depend_on_command_services() {
        noClasses()
                .that().resideInAnyPackage(APPLICATION_QUERY_SERVICE_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(APPLICATION_COMMAND_SERVICE_PACKAGES)
                .check(importedClasses());
    }

    @Test
    void command_services_do_not_depend_on_query_services_or_query_results() {
        noClasses()
                .that().resideInAnyPackage(APPLICATION_COMMAND_SERVICE_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..agent.application.service.query..",
                        "..agent.application.result.."
                )
                .check(importedClasses());
    }

    @Test
    void domain_and_kernel_remain_isolated() {
        JavaClasses classes = importedClasses();

        noClasses()
                .that().resideInAnyPackage(DOMAIN_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..agent.admin..",
                        "..agent.api..",
                        "..agent.application..",
                        "..agent.kernel..",
                        "..agent.infra..",
                        "..agent.mapper.."
                )
                .check(classes);

        noClasses()
                .that().resideInAnyPackage(KERNEL_PACKAGES)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..agent.admin..",
                        "..agent.api..",
                        "..agent.application..",
                        "..agent.entity..",
                        "..agent.model..",
                        "..agent.repository..",
                        "..agent.infra..",
                        "..agent.mapper.."
                )
                .check(classes);
    }

    private static String[] concat(String[]... packageGroups) {
        return Arrays.stream(packageGroups)
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }
}
