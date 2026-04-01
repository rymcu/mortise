package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.enumerate.MenuType;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.system.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.system.infra.persistence.entity.MenuPO;
import com.rymcu.mortise.system.mapper.MenuMapper;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;
import com.rymcu.mortise.system.query.MenuQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rymcu.mortise.system.infra.persistence.entity.table.MenuPOTableDef.MENU_PO;

/**
 * MyBatis-Flex 菜单仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository, MenuQueryService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> findMenusByUserId(Long userId) {
        return menuMapper.findMenusByIdUser(userId);
    }

    @Override
    public List<Menu> findMenusByIdUser(Long userId) {
        return findMenusByUserId(userId);
    }

    @Override
    public List<Menu> findLinksByUserIdAndParentId(Long userId, Long parentId) {
        return menuMapper.findLinksByUserIdAndParentId(userId, parentId);
    }

    @Override
    public List<Menu> findAllLinksByUserId(Long userId) {
        return menuMapper.findAllLinksByUserId(userId);
    }

    @Override
    public List<Link> findLinksByIdUser(Long userId) {
        return buildLinkTree(
                findAllLinksByUserId(userId).stream().collect(Collectors.groupingBy(Menu::getParentId)),
                0L
        );
    }

    @Override
    public PageResult<Menu> findMenus(PageQuery pageQuery, MenuSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(MENU_PO.ID, MENU_PO.LABEL, MENU_PO.PERMISSION, MENU_PO.HREF, MENU_PO.MENU_TYPE, MENU_PO.PARENT_ID,
                        MENU_PO.ICON, MENU_PO.SORT_NO, MENU_PO.STATUS, MENU_PO.CREATED_TIME)
                .where(MENU_PO.LABEL.like(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(MENU_PO.PARENT_ID.eq(search.getParentId(), Objects.nonNull(search.getParentId())))
                .orderBy(MENU_PO.SORT_NO.asc());
        Page<Menu> page = menuMapper.paginateAs(FlexPageMapper.toFlexPage(pageQuery), queryWrapper, Menu.class);
        return FlexPageMapper.toPageResult(page);
    }

    @Override
    public List<Menu> findTreeMenus(Long parentId) {
        return menuMapper.selectListByQueryAs(QueryWrapper.create()
                .select(MENU_PO.ID, MENU_PO.LABEL, MENU_PO.PERMISSION, MENU_PO.PARENT_ID, MENU_PO.SORT_NO,
                        MENU_PO.MENU_TYPE, MENU_PO.ICON, MENU_PO.HREF, MENU_PO.CREATED_TIME, MENU_PO.UPDATED_TIME, MENU_PO.STATUS)
                .where(MENU_PO.PARENT_ID.eq(parentId != null ? parentId : 0L))
                .and(MENU_PO.MENU_TYPE.ne(MenuType.BUTTON.ordinal()))
                .orderBy(MENU_PO.SORT_NO.asc()), Menu.class);
    }

    @Override
    public Menu findById(Long menuId) {
        return menuMapper.selectOneByQueryAs(QueryWrapper.create().where(MENU_PO.ID.eq(menuId)), Menu.class);
    }

    @Override
    public List<MenuTreeInfo> findMenuTree(MenuSearch search) {
        return buildMenuTree(search.getParentId());
    }

    @Override
    public boolean save(Menu menu) {
        MenuPO menuPO = PersistenceObjectMapper.copy(menu, MenuPO::new);
        boolean saved = menuMapper.insertSelective(menuPO) > 0;
        if (saved) {
            menu.setId(menuPO.getId());
        }
        return saved;
    }

    @Override
    public boolean saveAll(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return true;
        }
        return menuMapper.insertBatchSelective(PersistenceObjectMapper.copyList(menus, MenuPO::new)) == menus.size();
    }

    @Override
    public boolean update(Menu menu) {
        return menuMapper.update(PersistenceObjectMapper.copy(menu, MenuPO::new)) > 0;
    }

    @Override
    public boolean updateStatus(Long menuId, Integer status) {
        return UpdateChain.of(MenuPO.class)
                .set(MenuPO::getStatus, status)
                .where(MenuPO::getId).eq(menuId)
                .update();
    }

    @Override
    public boolean deleteById(Long menuId) {
        return menuMapper.deleteById(menuId) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> menuIds) {
        return menuMapper.deleteBatchByIds(menuIds) > 0;
    }

    @Override
    public long count() {
        return menuMapper.selectCountByQuery(QueryWrapper.create());
    }

    @Override
    public long countEnabled() {
        return menuMapper.selectCountByQuery(QueryWrapper.create().where(MENU_PO.STATUS.eq(Status.ENABLED.getCode())));
    }

    private List<MenuTreeInfo> buildMenuTree(Long parentId) {
        return findTreeMenus(parentId).stream()
                .map(menu -> {
                    MenuTreeInfo menuTreeInfo = new MenuTreeInfo();
                    menuTreeInfo.setId(menu.getId());
                    menuTreeInfo.setLabel(menu.getLabel());
                    menuTreeInfo.setPermission(menu.getPermission());
                    menuTreeInfo.setParentId(menu.getParentId());
                    menuTreeInfo.setSortNo(menu.getSortNo());
                    menuTreeInfo.setMenuType(menu.getMenuType());
                    menuTreeInfo.setIcon(menu.getIcon());
                    menuTreeInfo.setHref(menu.getHref());
                    menuTreeInfo.setStatus(menu.getStatus());
                    menuTreeInfo.setChildren(buildMenuTree(menu.getId()));
                    return menuTreeInfo;
                })
                .toList();
    }

    private List<Link> buildLinkTree(Map<Long, List<Menu>> menusByParentId, Long parentId) {
        List<Menu> children = menusByParentId.getOrDefault(parentId, Collections.emptyList());
        return children.stream()
                .map(menu -> {
                    Link link = new Link();
                    link.setId(menu.getId());
                    link.setLabel(menu.getLabel());
                    link.setParentId(menu.getParentId());
                    link.setTo(menu.getHref());
                    link.setIcon(menu.getIcon());
                    link.setSortNo(menu.getSortNo());
                    link.setStatus(menu.getStatus());
                    link.setChildren(buildLinkTree(menusByParentId, menu.getId()));
                    return link;
                })
                .toList();
    }
}
