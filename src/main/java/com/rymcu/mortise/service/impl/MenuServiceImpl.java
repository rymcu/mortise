package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.mapper.MenuMapper;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.MenuSearch;
import com.rymcu.mortise.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        return mapper.selectMenuListByIdRole(idRole);
    }

    @Override
    public List<Link> findLinksByIdUser(Long idUser) {
        return findLinkTreeMode(idUser, 0L);
    }

    @Override
    public List<Link> findMenus(MenuSearch search) {
        List<Menu> menus = mapper.selectMenuListByLabelAndParentId(null, search.getQuery(), search.getParentId());
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            Link link = new Link();
            link.setId(menu.getId());
            link.setLabel(menu.getLabel());
            link.setParentId(menu.getParentId());
            link.setIcon(menu.getIcon());
            link.setSortNo(menu.getSortNo());
            link.setStatus(menu.getStatus());
            MenuSearch menuSearch = new MenuSearch();
            menuSearch.setParentId(menu.getId());
            link.setChildren(findMenus(menuSearch));
            links.add(link);
        }
        return links;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMenu(Menu menu) {
        Menu oldMenu = mapper.selectOneById(menu.getId());
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
            return mapper.update(menu) > 0;
        }
        return mapper.insertSelective(menu) > 0;
    }

    @Override
    public List<Link> findChildrenMenus(Page<Link> page, MenuSearch search) {
        Page<Menu> menuPage = new Page<>(search.getPageNum(), search.getPageSize());
        List<Menu> menus = mapper.selectMenuListByLabelAndParentId(menuPage, search.getQuery(), search.getParentId());
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            links.add(convertLink(menu));
        }
        return links;
    }

    @Override
    public Boolean updateStatus(Long idMenu, Integer status) {
        Menu menu = UpdateEntity.of(Menu.class, idMenu);
        menu.setStatus(status);
        return mapper.update(menu) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idMenu, Integer delFlag) {
        return mapper.deleteById(idMenu) > 0;
    }

    @Override
    public Menu findById(Long idMenu) {
        return mapper.selectOneById(idMenu);
    }

    @Override
    public List<Link> findMenuTree(MenuSearch search) {
        return findMenuTreeMode(search.getParentId());
    }

    private List<Link> findMenuTreeMode(Long parentId) {
        List<Menu> menus = mapper.selectMenuListByParentId(parentId);
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            Link link = convertLink(menu);
            link.setChildren(findMenuTreeMode(menu.getId()));
            links.add(link);
        }
        return links;
    }

    private List<Link> findLinkTreeMode(Long idUser, long parentId) {
        List<Menu> menus = mapper.selectMenuListByIdUserAndParentId(idUser, parentId);
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            Link link = convertLink(menu);
            link.setChildren(findLinkTreeMode(idUser, menu.getId()));
            links.add(link);
        }
        return links;
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
