package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.handler.event.RegisterEvent;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.service.RoleService;
import com.rymcu.mortise.system.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2024/4/18 8:10.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler
 */
@Slf4j
@Component
public class RegisterHandler {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;

    @Async
    @TransactionalEventListener
    public void processRegisterEvent(RegisterEvent registerEvent) {
        Role role = roleService.findRoleByPermission("user");
        Set<Long> roleIds = new HashSet<>();
        roleIds.add(role.getId());
        userService.bindUserRole(new BindUserRoleInfo(registerEvent.getIdUser(), roleIds));
    }

}
