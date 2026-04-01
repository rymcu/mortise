package com.rymcu.mortise.system.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;

import java.util.List;

public interface MenuQueryService {

    PageResult<Menu> findMenus(PageQuery pageQuery, MenuSearch search);

    List<Menu> findMenusByIdUser(Long userId);

    List<Link> findLinksByIdUser(Long userId);

    Menu findById(Long menuId);

    List<MenuTreeInfo> findMenuTree(MenuSearch search);

    long count();

    long countEnabled();
}
