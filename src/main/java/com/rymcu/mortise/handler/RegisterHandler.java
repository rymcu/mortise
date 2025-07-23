package com.rymcu.mortise.handler;

import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.handler.event.RegisterEvent;
import com.rymcu.mortise.mapper.RoleMapper;
import com.rymcu.mortise.model.BindUserRoleInfo;
import com.rymcu.mortise.service.JavaMailService;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
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
    private RoleMapper roleMapper;
    @Resource
    private JavaMailService javaMailService;

    @Async
    @TransactionalEventListener
    public void processRegisterEvent(RegisterEvent registerEvent) {
        Role role = roleMapper.selectRoleByPermission("user");
        Set<Long> roleIds = new HashSet<>();
        roleIds.add(role.getId());
        userService.bindUserRole(new BindUserRoleInfo(registerEvent.getIdUser(), roleIds));
        try {
            javaMailService.sendInitialPassword(registerEvent.getAccount(), registerEvent.getCode());
        } catch (MessagingException e) {
            log.error("发送用户初始密码邮件失败！", e);
        }
    }

}
