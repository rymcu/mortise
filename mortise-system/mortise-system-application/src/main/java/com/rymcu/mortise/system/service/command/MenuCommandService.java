package com.rymcu.mortise.system.service.command;

import com.rymcu.mortise.system.entity.Menu;

import java.util.List;

public interface MenuCommandService {

    Boolean updateStatus(Long menuId, Integer status);

    Boolean deleteMenu(Long menuId);

    Boolean batchDeleteMenus(List<Long> menuIds);

    Long createMenu(Menu menu);

    Boolean updateMenu(Menu menu);
}
