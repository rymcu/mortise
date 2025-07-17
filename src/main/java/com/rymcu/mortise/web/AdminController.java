package com.rymcu.mortise.web;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.model.*;
import com.rymcu.mortise.service.MenuService;
import com.rymcu.mortise.service.RoleService;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
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
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;

    @GetMapping("/users")
    public GlobalResult<Page<UserInfo>> users(UserSearch search) {
        Page<UserInfo> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = userService.findUsers(page, search);
        return GlobalResult.success(page);
    }

    @GetMapping("/roles")
    public GlobalResult<Page<Role>> roles(RoleSearch search) {
        Page<Role> page = new Page<>(search.getPageNum(), search.getPageSize());
        return GlobalResult.success(roleService.findRoles(page, search));
    }

    @GetMapping("/menus")
    public GlobalResult<List<Link>> menus(MenuSearch search) {
        List<Link> list = menuService.findMenus(search);
        return GlobalResult.success(list);
    }

    @GetMapping("/children-menus")
    public GlobalResult<Page<Link>> childrenMenus(MenuSearch search) {
        Page<Link> page = new Page<>(search.getPageNum(), search.getPageSize());
        List<Link> list = menuService.findChildrenMenus(page, search);
        page.setRecords(list);
        return GlobalResult.success(page);
    }

}
