package com.rymcu.mortise.product.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.product.dto.ProductQueryParam;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.enums.ProductType;
import com.rymcu.mortise.product.mapper.ProductMapper;
import com.rymcu.mortise.product.service.ProductService;
import com.rymcu.mortise.product.spi.ProductLifecycleListener;
import com.rymcu.mortise.product.spi.ProductTypeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

import static com.rymcu.mortise.product.entity.table.ProductTableDef.PRODUCT;

/**
 * 产品目录服务实现
 *
 * @author ronger
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    /** SPI: 产品类型扩展提供者（可能为空） */
    private final List<ProductTypeProvider> typeProviders;

    /** SPI: 产品生命周期监听器（可能为空） */
    private final List<ProductLifecycleListener> lifecycleListeners;

    public ProductServiceImpl(
            Optional<List<ProductTypeProvider>> typeProviders,
            Optional<List<ProductLifecycleListener>> lifecycleListeners) {
        this.typeProviders = typeProviders.orElse(List.of());
        this.lifecycleListeners = lifecycleListeners.orElse(List.of());
    }

    @Override
    public Page<Product> pageByParam(Page<Product> page, ProductQueryParam param) {
        QueryWrapper qw = QueryWrapper.create();
        if (param != null) {
            if (StringUtils.hasText(param.keyword())) {
                qw.and(PRODUCT.TITLE.like(param.keyword())
                        .or(PRODUCT.SUBTITLE.like(param.keyword())));
            }
            if (StringUtils.hasText(param.productType())) {
                qw.and(PRODUCT.PRODUCT_TYPE.eq(param.productType()));
            }
            if (param.categoryId() != null) {
                qw.and(PRODUCT.CATEGORY_ID.eq(param.categoryId()));
            }
            if (param.status() != null) {
                qw.and(PRODUCT.STATUS.eq(param.status()));
            }
            if (param.isFeatured() != null) {
                qw.and(PRODUCT.IS_FEATURED.eq(param.isFeatured()));
            }
        }
        qw.orderBy(PRODUCT.SORT_NO.asc(), PRODUCT.CREATED_TIME.desc());
        return mapper.paginate(page, qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Product product = new Product();
        product.setStatus(status);
        product.setUpdatedTime(LocalDateTime.now());
        return mapper.updateByQuery(product,
                QueryWrapper.create().where(PRODUCT.ID.in(ids)));
    }

    @Override
    public List<Product> findByProductType(String productType) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PRODUCT.PRODUCT_TYPE.eq(productType))
                        .and(PRODUCT.STATUS.eq(1))
                        .orderBy(PRODUCT.SORT_NO.asc()));
    }

    @Override
    public Product findPublishedById(Long id) {
        if (id == null) {
            return null;
        }
        return mapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(PRODUCT.ID.eq(id))
                        .and(PRODUCT.STATUS.eq(1))
        );
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        Product existing = mapper.selectOneById(id);
        if (existing == null) {
            return false;
        }
        int oldStatus = existing.getStatus() != null ? existing.getStatus() : 0;

        Product product = UpdateEntity.of(Product.class, id);
        product.setStatus(status);
        product.setUpdatedTime(LocalDateTime.now());
        boolean success = mapper.update(product) > 0;

        // 触发 SPI 生命周期回调
        if (success) {
            existing.setStatus(status);
            lifecycleListeners.stream()
                    .sorted(Comparator.comparingInt(ProductLifecycleListener::getOrder))
                    .forEach(listener -> listener.onStatusChanged(existing, oldStatus, status));
        }
        return success;
    }

    @Override
    public Map<String, String> getAllProductTypes() {
        Map<String, String> allTypes = new LinkedHashMap<>();
        // 内置类型
        for (ProductType type : ProductType.values()) {
            allTypes.put(type.getCode(), type.getDescription());
        }
        // SPI 注册的自定义类型
        typeProviders.stream()
                .sorted(Comparator.comparingInt(ProductTypeProvider::getOrder))
                .forEach(provider -> allTypes.putAll(provider.getProductTypes()));
        return allTypes;
    }
}
