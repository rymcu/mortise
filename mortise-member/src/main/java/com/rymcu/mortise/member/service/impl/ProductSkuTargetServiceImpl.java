package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.ProductSkuTarget;
import com.rymcu.mortise.member.mapper.ProductSkuTargetMapper;
import com.rymcu.mortise.member.service.ProductSkuTargetService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rymcu.mortise.member.entity.table.ProductSkuTargetTableDef.PRODUCT_SKU_TARGET;

/**
 * Created on 2025/11/20 11:02.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class ProductSkuTargetServiceImpl extends ServiceImpl<ProductSkuTargetMapper, ProductSkuTarget> implements ProductSkuTargetService {

    @Override
    public List<ProductSkuTarget> findBySkuId(Long skuId) {
        return mapper.selectListByQuery(
                QueryWrapper.create().where(PRODUCT_SKU_TARGET.PRODUCT_SKU_ID.eq(skuId))
        );
    }

    @Override
    public List<ProductSkuTarget> findBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectListByQuery(
                QueryWrapper.create().where(PRODUCT_SKU_TARGET.PRODUCT_SKU_ID.in(skuIds))
        );
    }
}
