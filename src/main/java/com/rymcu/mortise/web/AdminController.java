package com.rymcu.mortise.web;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.model.*;
import com.rymcu.mortise.service.MenuService;
import com.rymcu.mortise.service.RoleService;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresRoles;
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
@RequiresRoles("admin")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;

    @GetMapping("/users")
    public GlobalResult<PageInfo<UserInfo>> users(UserSearch search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        List<UserInfo> list = userService.findUsers(search);
        PageInfo<UserInfo> pageInfo = new PageInfo<>(list);
        return GlobalResultGenerator.genSuccessResult(pageInfo);
    }

    @GetMapping("/roles")
    public GlobalResult<PageInfo<Role>> roles(RoleSearch search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        List<Role> list = roleService.findRoles(search);
        PageInfo<Role> pageInfo = new PageInfo<>(list);
        return GlobalResultGenerator.genSuccessResult(pageInfo);
    }

    @GetMapping("/menus")
    public GlobalResult<List<Link>> menus(MenuSearch search) {
        List<Link> list = menuService.findMenus(search);
        return GlobalResultGenerator.genSuccessResult(list);
    }

    @GetMapping("/children-menus")
    public GlobalResult<PageInfo<Link>> childrenMenus(MenuSearch search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        List<Link> list = menuService.findChildrenMenus(search);
        PageInfo<Link> pageInfo = new PageInfo<>(list);
        return GlobalResultGenerator.genSuccessResult(pageInfo);
    }

}
