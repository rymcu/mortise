package com.rymcu.mortise.system.service;

import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;

import java.util.List;

/**
 * Created on 2024/4/17 9:49.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface MenuService {
    List<Menu> findMenusByIdUser(Long idUser);

    List<Link> findLinksByIdUser(Long idUser);

    PageResult<Menu> findMenus(PageQuery pageQuery, MenuSearch search);

    Menu findById(Long idMenu);

    Boolean updateStatus(Long idMenu, Integer status);

    Boolean deleteMenu(Long idMenu);

    List<MenuTreeInfo> findMenuTree(MenuSearch search);

    Boolean batchDeleteMenus(List<Long> idMenuList);

    Long createMenu(Menu menu);

    Boolean updateMenu(Menu menu);

    long count();

    long countEnabled();
}
