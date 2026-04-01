package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.system.controller.request.MenuUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.entity.Menu;

import java.util.List;

/**
 * 管理端菜单视图装配器。
 */
public final class MenuAdminAssembler {

    private MenuAdminAssembler() {
    }

    public static List<MenuVO> toMenuVOs(List<Menu> menus) {
        return menus.stream()
                .map(MenuAdminAssembler::toMenuVO)
                .toList();
    }

    public static MenuVO toMenuVO(Menu menu) {
        MenuVO menuVO = new MenuVO();
        menuVO.setId(menu.getId());
        menuVO.setLabel(menu.getLabel());
        menuVO.setPermission(menu.getPermission());
        menuVO.setIcon(menu.getIcon());
        menuVO.setHref(menu.getHref());
        menuVO.setStatus(menu.getStatus());
        menuVO.setDelFlag(menu.getDelFlag());
        menuVO.setMenuType(menu.getMenuType());
        menuVO.setSortNo(menu.getSortNo());
        menuVO.setParentId(menu.getParentId());
        menuVO.setCreatedTime(menu.getCreatedTime());
        menuVO.setUpdatedTime(menu.getUpdatedTime());
        return menuVO;
    }

    public static Menu toMenu(MenuUpsertRequest request) {
        Menu menu = new Menu();
        menu.setLabel(request.getLabel());
        menu.setPermission(request.getPermission());
        menu.setIcon(request.getIcon());
        menu.setHref(request.getHref());
        menu.setStatus(request.getStatus());
        menu.setMenuType(request.getMenuType());
        menu.setSortNo(request.getSortNo());
        menu.setParentId(request.getParentId());
        return menu;
    }
}
