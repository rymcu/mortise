package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.mapper.MenuMapper;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.MenuSearch;
import com.rymcu.mortise.service.MenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.entity.table.MenuTableDef.MENU;

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
        // 原 SQL: select id, label, permission from mortise_menu tm where del_flag = 0 
        // and exists(select 1 from mortise_role_menu trm where trm.id_mortise_menu = tm.id and trm.id_mortise_role = #{idRole})
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(MENU.ID, MENU.LABEL, MENU.PERMISSION)
                .where(MENU.DEL_FLAG.eq(0))
                .and("EXISTS (SELECT 1 FROM mortise_role_menu trm WHERE trm.id_mortise_menu = mortise_menu.id AND trm.id_mortise_role = {0})", idRole);
        return mapper.selectListByQuery(queryWrapper);
    }

    @Override
    public List<Menu> findMenusByIdUser(Long idUser) {
        // 原 SQL: select id, label, permission from mortise_menu tm where del_flag = 0
        // and exists(select 1 from mortise_role_menu trm where trm.id_mortise_menu = tm.id 
        // and exists(select 1 from mortise_user_role tur where tur.id_mortise_role = trm.id_mortise_role and tur.id_mortise_user = #{idUser}))
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(MENU.ID, MENU.LABEL, MENU.PERMISSION)
                .where(MENU.DEL_FLAG.eq(0))
                .and("EXISTS (SELECT 1 FROM mortise_role_menu trm WHERE trm.id_mortise_menu = mortise_menu.id " +
                     "AND EXISTS (SELECT 1 FROM mortise_user_role tur WHERE tur.id_mortise_role = trm.id_mortise_role AND tur.id_mortise_user = {0}))", idUser);
        return mapper.selectListByQuery(queryWrapper);
    }

    @Override
    public List<Link> findLinksByIdUser(Long idUser) {
        return findLinkTreeMode(idUser, 0L);
    }

    @Override
    public List<Link> findMenus(MenuSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(MENU.LABEL.like(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(MENU.PARENT_ID.eq(search.getParentId(), Objects.nonNull(search.getParentId())))
                .orderBy(MENU.SORT_NO.asc());
        List<Menu> menus = mapper.selectListByQuery(queryWrapper);
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
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(MENU.LABEL.like(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(MENU.PARENT_ID.eq(search.getParentId(), Objects.nonNull(search.getParentId())))
                .orderBy(MENU.SORT_NO.asc());
        Page<Menu> menuPageResult = mapper.paginate(menuPage, queryWrapper);
        List<Menu> menus = menuPageResult.getRecords();
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
    public Boolean deleteMenu(Long idMenu) {
        return mapper.deleteById(idMenu) > 0;
    }

    @Override
    public Boolean batchDeleteMenus(List<Long> idMenuList) {
        if (idMenuList == null || idMenuList.isEmpty()) {
            return false;
        }
        return mapper.deleteBatchByIds(idMenuList) > 0;
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
        // 原 SQL: select id, label, permission, parent_id, sort_no, menu_type, icon, href, created_time, updated_time, status
        // from mortise_menu where parent_id = #{parentId} or (parentId is null and parent_id = 0)
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(MENU.ID, MENU.LABEL, MENU.PERMISSION, MENU.PARENT_ID, MENU.SORT_NO, 
                        MENU.MENU_TYPE, MENU.ICON, MENU.HREF, MENU.CREATED_TIME, MENU.UPDATED_TIME, MENU.STATUS)
                .where(MENU.PARENT_ID.eq(parentId != null ? parentId : 0L))
                .orderBy(MENU.SORT_NO.asc());
        List<Menu> menus = mapper.selectListByQuery(queryWrapper);
        List<Link> links = new ArrayList<>();
        for (Menu menu : menus) {
            Link link = convertLink(menu);
            link.setChildren(findMenuTreeMode(menu.getId()));
            links.add(link);
        }
        return links;
    }

    private List<Link> findLinkTreeMode(Long idUser, long parentId) {
        // 原 SQL: select id, label, permission, parent_id, sort_no, menu_type, icon, href from mortise_menu tm
        // where del_flag = 0 and menu_type = 0 and parent_id = #{parentId}
        // and exists(select 1 from mortise_role_menu trm where trm.id_mortise_menu = tm.id 
        // and exists(select 1 from mortise_user_role tur where tur.id_mortise_role = trm.id_mortise_role and tur.id_mortise_user = #{idUser}))
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(MENU.ID, MENU.LABEL, MENU.PERMISSION, MENU.PARENT_ID, MENU.SORT_NO, 
                        MENU.MENU_TYPE, MENU.ICON, MENU.HREF)
                .where(MENU.DEL_FLAG.eq(0))
                .and(MENU.MENU_TYPE.eq(0))
                .and(MENU.PARENT_ID.eq(parentId))
                .and("EXISTS (SELECT 1 FROM mortise_role_menu trm WHERE trm.id_mortise_menu = mortise_menu.id " +
                     "AND EXISTS (SELECT 1 FROM mortise_user_role tur WHERE tur.id_mortise_role = trm.id_mortise_role AND tur.id_mortise_user = {0}))", idUser)
                .orderBy(MENU.SORT_NO.asc());
        List<Menu> menus = mapper.selectListByQuery(queryWrapper);
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
