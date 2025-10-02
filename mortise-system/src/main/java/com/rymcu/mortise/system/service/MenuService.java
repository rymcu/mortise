package com.rymcu.mortise.system.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.system.model.MenuSearch;

import java.util.List;

/**
 * Created on 2024/4/17 9:49.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface MenuService extends IService<Menu> {
    List<Menu> findMenusByIdRole(Long idRole);

    List<Menu> findMenusByIdUser(Long idUser);

    List<Link> findLinksByIdUser(Long idUser);

    List<Link> findMenus(MenuSearch search);

    Boolean saveMenu(Menu menu);

    List<Menu> findChildrenMenus(Page<Menu> page, MenuSearch search);

    Boolean updateStatus(Long idMenu, Integer status);

    Boolean deleteMenu(Long idMenu);

    Menu findById(Long idMenu);

    List<Link> findMenuTree(MenuSearch search);

    Boolean batchDeleteMenus(List<Long> idMenuList);
}
