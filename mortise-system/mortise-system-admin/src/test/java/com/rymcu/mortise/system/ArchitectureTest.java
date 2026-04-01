package com.rymcu.mortise.system;

import com.rymcu.mortise.test.support.arch.AbstractLayeredArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest extends AbstractLayeredArchitectureTest {

    @Override
    protected String basePackage() {
        return "com.rymcu.mortise.system";
    }

    @Test
    void system_module_respects_current_layering_contract() {
        assertCurrentLayeredArchitecture();
    }

    @Test
    void refactored_application_services_do_not_reach_back_to_mapper_runtime() {
        JavaClasses classes = importedClasses();

        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.MenuServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.DictServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.DictTypeServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.RoleServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.UserServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.UserDetailsServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.UserOAuth2BindingServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.SiteConfigServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.SystemInitServiceImpl")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.rymcu.mortise.system.mapper..",
                        "com.mybatisflex.core.query..",
                        "com.mybatisflex.core.update..",
                        "com.mybatisflex.core.util..",
                        "com.mybatisflex.spring.service.impl.."
                )
                .check(classes);
    }

    @Test
    void core_admin_controllers_use_facade_instead_of_service_query_entity_or_assembler() {
        JavaClasses classes = importedClasses();

        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.controller.UserController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.RoleController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.MenuController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.DictController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.DictTypeController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.AuthController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.DashboardController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.LogController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.SiteConfigController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.SystemCacheController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.SystemInitController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.NotificationChannelConfigController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.SmsAuthController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.SystemFileController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.oauth2.Oauth2ClientConfigController")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.oauth2.Oauth2QrcodeController")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.rymcu.mortise.system.service..",
                        "com.rymcu.mortise.system.query..",
                        "com.rymcu.mortise.system.entity..",
                        "com.rymcu.mortise.system.controller.assembler..",
                        "com.rymcu.mortise.auth.service..",
                        "com.rymcu.mortise.auth.entity..",
                        "com.rymcu.mortise.notification.service..",
                        "com.rymcu.mortise.file.service..",
                        "com.rymcu.mortise.file.mapper..",
                        "com.rymcu.mortise.file.entity.."
                )
                .check(classes);
    }

    @Test
    void core_admin_facades_do_not_depend_on_controllers() {
        noClasses()
                .that().resideInAnyPackage("com.rymcu.mortise.system.controller.facade..")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.UserController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.RoleController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.MenuController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.DictController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.DictTypeController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.AuthController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.DashboardController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.LogController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.SiteConfigController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.SystemCacheController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.SystemInitController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.NotificationChannelConfigController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.SmsAuthController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.SystemFileController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.oauth2.Oauth2ClientConfigController")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.controller.oauth2.Oauth2QrcodeController")
                .check(importedClasses());
    }

    @Test
    void dict_facades_use_command_and_query_contracts_instead_of_mixed_services() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.DictAdminFacadeImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.DictTypeAdminFacadeImpl")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.DictService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.DictTypeService")
                .check(importedClasses());
    }

    @Test
    void menu_facade_uses_command_and_query_contracts_instead_of_mixed_service() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.MenuAdminFacadeImpl")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.MenuService")
                .check(importedClasses());
    }

    @Test
    void role_and_user_facades_use_command_and_query_contracts_instead_of_mixed_services() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.RoleAdminFacadeImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.UserAdminFacadeImpl")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.RoleService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.UserService")
                .check(importedClasses());
    }

    @Test
    void dashboard_facade_uses_query_contracts_for_stats() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.DashboardAdminFacadeImpl")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.MenuService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.RoleService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.UserService")
                .check(importedClasses());
    }

    @Test
    void auth_facade_uses_user_command_and_query_contracts_instead_of_mixed_user_service() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.controller.facade.impl.AuthAdminFacadeImpl")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.UserService")
                .check(importedClasses());
    }

    @Test
    void dict_admin_support_components_use_query_contract_instead_of_mixed_dict_service() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.serializer.DictSerializer")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.annotation.DictAnnotationIntrospector")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.config.DictJacksonConfigurer")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.DictService")
                .check(importedClasses());
    }

    @Test
    void refactored_application_internal_components_use_query_or_command_contracts_instead_of_mixed_services() {
        noClasses()
                .that().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.PermissionServiceImpl")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.handler.RegisterHandler")
                .or().haveFullyQualifiedName("com.rymcu.mortise.system.service.impl.UserCacheServiceImpl")
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.RoleService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.UserService")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("com.rymcu.mortise.system.service.MenuService")
                .check(importedClasses());
    }
}
