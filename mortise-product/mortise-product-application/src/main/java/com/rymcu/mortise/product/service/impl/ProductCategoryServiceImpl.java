package com.rymcu.mortise.product.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.mapper.ProductCategoryMapper;
import com.rymcu.mortise.product.service.ProductCategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rymcu.mortise.product.entity.table.ProductCategoryTableDef.PRODUCT_CATEGORY;

/**
 * 产品分类服务实现
 *
 * @author ronger
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements ProductCategoryService {

    @Override
    public List<ProductCategory> getTree() {
        // 仅查询激活的分类
        List<ProductCategory> all = mapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PRODUCT_CATEGORY.IS_ACTIVE.eq(true))
                        .orderBy(PRODUCT_CATEGORY.SORT_NO.asc(), PRODUCT_CATEGORY.CREATED_TIME.asc()));
        return buildTree(all);
    }

    @Override
    public List<ProductCategory> getFullTree() {
        // 后台管理：查询全部（含禁用）
        List<ProductCategory> all = mapper.selectListByQuery(
                QueryWrapper.create()
                        .orderBy(PRODUCT_CATEGORY.SORT_NO.asc(), PRODUCT_CATEGORY.CREATED_TIME.asc()));
        return buildTree(all);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        ProductCategory category = UpdateEntity.of(ProductCategory.class, id);
        category.setStatus(status);
        category.setIsActive(status == Status.ENABLED.getCode());
        category.setUpdatedTime(LocalDateTime.now());
        return mapper.update(category) > 0;
    }

    /**
     * 将平铺列表组装为树形结构
     * 根节点条件：parentId 为 null
     */
    private List<ProductCategory> buildTree(List<ProductCategory> all) {
        Map<Long, List<ProductCategory>> grouped = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ProductCategory::getParentId));

        List<ProductCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toCollection(ArrayList::new));

        roots.forEach(root -> fillChildren(root, grouped));
        return roots;
    }

    private void fillChildren(ProductCategory node, Map<Long, List<ProductCategory>> grouped) {
        List<ProductCategory> children = grouped.getOrDefault(node.getId(), List.of());
        node.setChildren(children);
        children.forEach(child -> fillChildren(child, grouped));
    }
}
