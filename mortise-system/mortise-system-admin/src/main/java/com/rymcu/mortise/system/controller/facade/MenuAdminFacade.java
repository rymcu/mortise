package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.request.MenuStatusRequest;
import com.rymcu.mortise.system.controller.request.MenuUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;

import java.util.List;

public interface MenuAdminFacade {

    PageResult<MenuVO> listMenus(MenuSearch search);

    MenuVO getMenuById(Long menuId);

    Long createMenu(MenuUpsertRequest request);

    Boolean updateMenu(Long menuId, MenuUpsertRequest request);

    Boolean updateMenuStatus(Long menuId, MenuStatusRequest request);

    Boolean deleteMenu(Long menuId);

    List<MenuTreeInfo> getMenuTree(MenuSearch search);

    Boolean batchDeleteMenus(BatchUpdateInfo batchUpdateInfo);
}
