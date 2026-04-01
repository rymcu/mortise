package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;
import com.rymcu.mortise.system.query.MenuQueryService;
import com.rymcu.mortise.system.repository.MenuRepository;
import com.rymcu.mortise.system.service.command.MenuCommandService;
import com.rymcu.mortise.system.service.MenuService;
import com.rymcu.mortise.system.service.SystemCacheService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 菜单应用服务
 */
@Service
public class MenuServiceImpl implements MenuService, MenuCommandService {

    @Resource
    private MenuRepository menuRepository;

    @Resource
    private SystemCacheService systemCacheService;
    @Resource
    private MenuQueryService menuQueryService;

    @Override
    public List<Menu> findMenusByIdUser(Long idUser) {
        return menuRepository.findMenusByUserId(idUser);
    }

    @Override
    public List<Link> findLinksByIdUser(Long idUser) {
        return buildLinkTree(
                menuRepository.findAllLinksByUserId(idUser).stream().collect(Collectors.groupingBy(Menu::getParentId)),
                0L
        );
    }

    @Override
    public PageResult<Menu> findMenus(PageQuery pageQuery, MenuSearch search) {
        return menuQueryService.findMenus(pageQuery, search);
    }

    @Override
    public Menu findById(Long idMenu) {
        return menuRepository.findById(idMenu);
    }

    @Override
    public Boolean updateStatus(Long idMenu, Integer status) {
        return menuRepository.updateStatus(idMenu, status);
    }

    @Override
    public Boolean deleteMenu(Long idMenu) {
        boolean result = menuRepository.deleteById(idMenu);
        if (result) {
            systemCacheService.cacheMenuCount(count());
        }
        return result;
    }

    @Override
    public List<MenuTreeInfo> findMenuTree(MenuSearch search) {
        return buildMenuTree(search.getParentId() != null ? search.getParentId() : 0L);
    }

    @Override
    public Boolean batchDeleteMenus(List<Long> idMenuList) {
        if (idMenuList == null || idMenuList.isEmpty()) {
            return false;
        }
        boolean result = menuRepository.deleteByIds(idMenuList);
        if (result) {
            systemCacheService.cacheMenuCount(count());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(Menu menu) {
        menuRepository.save(menu);
        systemCacheService.cacheMenuCount(count());
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMenu(Menu menu) {
        Menu oldMenu = menuRepository.findById(menu.getId());
        if (Objects.isNull(oldMenu)) {
            throw new ServiceException("数据不存在");
        }
        oldMenu.setLabel(menu.getLabel());
        oldMenu.setPermission(menu.getPermission());
        oldMenu.setIcon(menu.getIcon());
        oldMenu.setHref(menu.getHref());
        oldMenu.setStatus(menu.getStatus());
        oldMenu.setMenuType(menu.getMenuType());
        oldMenu.setSortNo(menu.getSortNo());
        oldMenu.setParentId(menu.getParentId());
        oldMenu.setUpdatedTime(LocalDateTime.now());
        return menuRepository.update(oldMenu);
    }

    @Override
    public long count() {
        return menuRepository.count();
    }

    @Override
    public long countEnabled() {
        return menuRepository.countEnabled();
    }

    private List<MenuTreeInfo> buildMenuTree(Long parentId) {
        List<Menu> menus = menuRepository.findTreeMenus(parentId);
        List<MenuTreeInfo> result = new ArrayList<>(menus.size());
        for (Menu menu : menus) {
            MenuTreeInfo treeInfo = new MenuTreeInfo();
            BeanUtils.copyProperties(menu, treeInfo);
            treeInfo.setChildren(buildMenuTree(menu.getId()));
            result.add(treeInfo);
        }
        return result;
    }

    private List<Link> buildLinkTree(Map<Long, List<Menu>> menusByParentId, Long parentId) {
        List<Menu> children = menusByParentId.getOrDefault(parentId, Collections.emptyList());
        List<Link> result = new ArrayList<>(children.size());
        for (Menu menu : children) {
            Link link = convertLink(menu);
            link.setChildren(buildLinkTree(menusByParentId, menu.getId()));
            result.add(link);
        }
        return result;
    }

    private static Link convertLink(Menu menu) {
        Link link = new Link();
        link.setId(menu.getId());
        link.setLabel(menu.getLabel());
        link.setParentId(menu.getParentId());
        link.setTo(menu.getHref());
        link.setIcon(menu.getIcon());
        link.setSortNo(menu.getSortNo());
        link.setStatus(menu.getStatus());
        return link;
    }
}
