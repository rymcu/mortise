package com.rymcu.mortise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.MenuSearch;

import java.util.List;

/**
 * Created on 2024/4/17 9:49.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface MenuService {
    List<Menu> findMenusByIdRole(Long idRole);

    List<Link> findLinksByIdUser(Long idUser);

    List<Link> findMenus(MenuSearch search);

    Boolean saveMenu(Menu menu);

    List<Link> findChildrenMenus(Page<Link> page, MenuSearch search);

    Boolean updateStatus(Long idMenu, Integer status);

    Boolean updateDelFlag(Long idMenu, Integer delFlag);

    Menu findById(Long idMenu);
}
