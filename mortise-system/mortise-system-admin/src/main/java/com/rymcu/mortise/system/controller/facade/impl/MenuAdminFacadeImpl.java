package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.assembler.MenuAdminAssembler;
import com.rymcu.mortise.system.controller.facade.MenuAdminFacade;
import com.rymcu.mortise.system.controller.request.MenuStatusRequest;
import com.rymcu.mortise.system.controller.request.MenuUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;
import com.rymcu.mortise.system.query.MenuQueryService;
import com.rymcu.mortise.system.service.command.MenuCommandService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuAdminFacadeImpl implements MenuAdminFacade {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    public MenuAdminFacadeImpl(MenuCommandService menuCommandService, MenuQueryService menuQueryService) {
        this.menuCommandService = menuCommandService;
        this.menuQueryService = menuQueryService;
    }

    @Override
    public PageResult<MenuVO> listMenus(MenuSearch search) {
        PageResult<Menu> result = menuQueryService.findMenus(PageQuery.of(search.getPageNum(), search.getPageSize()), search);
        return result.map(MenuAdminAssembler::toMenuVO);
    }

    @Override
    public MenuVO getMenuById(Long menuId) {
        return MenuAdminAssembler.toMenuVO(menuQueryService.findById(menuId));
    }

    @Override
    public Long createMenu(MenuUpsertRequest request) {
        return menuCommandService.createMenu(MenuAdminAssembler.toMenu(request));
    }

    @Override
    public Boolean updateMenu(Long menuId, MenuUpsertRequest request) {
        Menu menu = MenuAdminAssembler.toMenu(request);
        menu.setId(menuId);
        return menuCommandService.updateMenu(menu);
    }

    @Override
    public Boolean updateMenuStatus(Long menuId, MenuStatusRequest request) {
        return menuCommandService.updateStatus(menuId, request.getStatus());
    }

    @Override
    public Boolean deleteMenu(Long menuId) {
        return menuCommandService.deleteMenu(menuId);
    }

    @Override
    public List<MenuTreeInfo> getMenuTree(MenuSearch search) {
        return menuQueryService.findMenuTree(search);
    }

    @Override
    public Boolean batchDeleteMenus(BatchUpdateInfo batchUpdateInfo) {
        return menuCommandService.batchDeleteMenus(batchUpdateInfo.getIds());
    }
}
