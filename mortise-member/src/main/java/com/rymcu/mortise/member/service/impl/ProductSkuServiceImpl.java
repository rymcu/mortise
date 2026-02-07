package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.ProductSku;
import com.rymcu.mortise.member.mapper.ProductSkuMapper;
import com.rymcu.mortise.member.service.ProductSkuService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rymcu.mortise.member.entity.table.ProductSkuTableDef.PRODUCT_SKU;

/**
 * Created on 2025/11/20 11:01.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    @Override
    public List<ProductSku> findByProductId(Long productId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PRODUCT_SKU.PRODUCT_ID.eq(productId))
                        .orderBy(PRODUCT_SKU.CREATED_TIME.asc())
        );
    }
}
