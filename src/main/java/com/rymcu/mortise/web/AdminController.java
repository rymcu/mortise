package com.rymcu.mortise.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.model.*;
import com.rymcu.mortise.service.MenuService;
import com.rymcu.mortise.service.RoleService;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created on 2024/4/19 8:44.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('admin')")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;

    @GetMapping("/users")
    public GlobalResult<IPage<UserInfo>> users(UserSearch search) {
        Page<UserInfo> page = new Page<>(search.getPageNum(), search.getPageSize());
        List<UserInfo> list = userService.findUsers(page, search);
        page.setRecords(list);
        return GlobalResultGenerator.genSuccessResult(page);
    }

    @GetMapping("/roles")
    public GlobalResult<IPage<Role>> roles(RoleSearch search) {
        Page<Role> page = new Page<>(search.getPageNum(), search.getPageSize());
        List<Role> list = roleService.findRoles(page, search);
        page.setRecords(list);
        return GlobalResultGenerator.genSuccessResult(page);
    }

    @GetMapping("/menus")
    public GlobalResult<List<Link>> menus(MenuSearch search) {
        List<Link> list = menuService.findMenus(search);
        return GlobalResultGenerator.genSuccessResult(list);
    }

    @GetMapping("/children-menus")
    public GlobalResult<IPage<Link>> childrenMenus(MenuSearch search) {
        Page<Link> page = new Page<>(search.getPageNum(), search.getPageSize());
        List<Link> list = menuService.findChildrenMenus(page, search);
        page.setRecords(list);
        return GlobalResultGenerator.genSuccessResult(page);
    }

}
