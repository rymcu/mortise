package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.handler.event.RegisterEvent;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.query.RoleQueryService;
import com.rymcu.mortise.system.service.command.UserCommandService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private UserCommandService userCommandService;
    @Resource
    private RoleQueryService roleQueryService;

    @Async
    @TransactionalEventListener
    public void processRegisterEvent(RegisterEvent registerEvent) {
        // 获取默认角色，不再使用硬编码的 "user"
        List<Role> roles = roleQueryService.findDefaultRole();
        if (roles == null) {
            log.warn("未找到默认角色，用户 {} 注册后未分配角色", registerEvent.getIdUser());
            return;
        }
        Set<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
        userCommandService.bindUserRole(new BindUserRoleInfo(registerEvent.getIdUser(), roleIds));
    }

}
