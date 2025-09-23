package com.rymcu.mortise.web;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.enumerate.DelFlag;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.MenuSearch;
import com.rymcu.mortise.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created on 2024/8/10 17:28.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/admin/menu")
@PreAuthorize("hasRole('admin')")
public class MenuController {
    @Resource
    private MenuService menuService;

    @PostMapping("/update-status")
    public GlobalResult<Boolean> updateMenuStatus(@RequestBody Menu menu) {
        return GlobalResult.success(menuService.updateStatus(menu.getId(), menu.getStatus()));
    }

    @GetMapping("/detail/{idMenu}")
    public GlobalResult<Menu> menu(@PathVariable Long idMenu) {
        return GlobalResult.success(menuService.findById(idMenu));
    }

    @PostMapping("/post")
    public GlobalResult<Boolean> addMenu(@RequestBody Menu menu) {
        return GlobalResult.success(menuService.saveMenu(menu));
    }

    @PutMapping("/post")
    public GlobalResult<Boolean> updateMenu(@RequestBody Menu menu) {
        return GlobalResult.success(menuService.saveMenu(menu));
    }

    @DeleteMapping("/update-del-flag")
    public GlobalResult<Boolean> updateDelFlag(Long idMenu) {
        return GlobalResult.success(menuService.updateDelFlag(idMenu, DelFlag.DELETED.ordinal()));
    }

    @GetMapping("/tree")
    public GlobalResult<List<Link>> tree(MenuSearch search) {
        List<Link> menus = menuService.findMenuTree(search);
        return GlobalResult.success(menus);
    }
}
