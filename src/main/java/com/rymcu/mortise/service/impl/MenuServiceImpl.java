package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.mapper.MenuMapper;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.MenuSearch;
import com.rymcu.mortise.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2024/4/17 9:49.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<Menu> findMenusByIdRole(Long idRole) {
        return baseMapper.selectMenuListByIdRole(idRole);
    }

    @Override
    public List<Link> findLinksByIdUser(Long idUser) {
        return findLinkTreeMode(idUser, 0L);
    }

    @Override
    public List<Link> findMenus(MenuSearch search) {
        List<Menu> menus = baseMapper.selectMenuListByLabelAndParentId(null, search.getQuery(), search.getParentId());
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            Link link = new Link();
            link.setId(menu.getIdMenu());
            link.setLabel(menu.getLabel());
            link.setParentId(menu.getParentId());
            link.setIcon(menu.getIcon());
            link.setSortNo(menu.getSortNo());
            link.setStatus(menu.getStatus());
            MenuSearch menuSearch = new MenuSearch();
            menuSearch.setParentId(menu.getIdMenu());
            link.setChildren(findMenus(menuSearch));
            links.add(link);
        }
        return links;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMenu(Menu menu) {
        Menu oldMenu = baseMapper.selectById(menu.getIdMenu());
        if (Objects.nonNull(oldMenu)) {
            oldMenu.setLabel(menu.getLabel());
            oldMenu.setPermission(menu.getPermission());
            oldMenu.setIcon(menu.getIcon());
            oldMenu.setHref(menu.getHref());
            oldMenu.setStatus(menu.getStatus());
            oldMenu.setMenuType(menu.getMenuType());
            oldMenu.setSortNo(menu.getSortNo());
            oldMenu.setParentId(menu.getParentId());
            oldMenu.setUpdatedTime(menu.getUpdatedTime());
        }
        oldMenu.setCreatedTime(LocalDateTime.now());
        return baseMapper.insertOrUpdate(menu);
    }

    @Override
    public List<Link> findChildrenMenus(Page<Link> page, MenuSearch search) {
        Page<Menu> menuPage = new Page<>(search.getPageNum(), search.getPageSize());
        List<Menu> menus = baseMapper.selectMenuListByLabelAndParentId(menuPage, search.getQuery(), search.getParentId());
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            links.add(convertLink(menu));
        }
        return links;
    }

    @Override
    public Boolean updateStatus(Long idMenu, Integer status) {
        return baseMapper.updateStatusByIdMenu(idMenu, status) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idMenu, Integer delFlag) {
        return baseMapper.updateDelFlag(idMenu, delFlag) > 0;
    }

    @Override
    public Menu findById(Long idMenu) {
        return baseMapper.selectById(idMenu);
    }

    private List<Link> findLinkTreeMode(Long idUser, long parentId) {
        List<Menu> menus = baseMapper.selectMenuListByIdUserAndParentId(idUser, parentId);
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            Link link = convertLink(menu);
            link.setChildren(findLinkTreeMode(idUser, menu.getIdMenu()));
            links.add(link);
        }
        return links;
    }

    private static Link convertLink(Menu menu) {
        Link link = new Link();
        link.setId(menu.getIdMenu());
        link.setLabel(menu.getLabel());
        link.setParentId(menu.getParentId());
        link.setTo(menu.getHref());
        link.setIcon(menu.getIcon());
        link.setSortNo(menu.getSortNo());
        link.setStatus(menu.getStatus());
        return link;
    }
}
