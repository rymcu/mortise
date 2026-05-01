package com.rymcu.mortise.product.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.product.entity.ProductSkuTarget;
import com.rymcu.mortise.product.mapper.ProductSkuTargetMapper;
import com.rymcu.mortise.product.service.ProductSkuTargetService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.rymcu.mortise.product.entity.table.ProductSkuTargetTableDef.PRODUCT_SKU_TARGET;

@Service
public class ProductSkuTargetServiceImpl extends ServiceImpl<ProductSkuTargetMapper, ProductSkuTarget>
        implements ProductSkuTargetService {

    @Override
    public List<ProductSkuTarget> findByProductSkuId(Long productSkuId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PRODUCT_SKU_TARGET.PRODUCT_SKU_ID.eq(productSkuId))
                        .orderBy(PRODUCT_SKU_TARGET.CREATED_TIME.asc())
        );
    }

    @Override
    public List<ProductSkuTarget> findByProductSkuIds(Collection<Long> productSkuIds) {
        if (productSkuIds == null || productSkuIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PRODUCT_SKU_TARGET.PRODUCT_SKU_ID.in(productSkuIds))
                        .orderBy(PRODUCT_SKU_TARGET.PRODUCT_SKU_ID.asc(), PRODUCT_SKU_TARGET.CREATED_TIME.asc())
        );
    }
}
