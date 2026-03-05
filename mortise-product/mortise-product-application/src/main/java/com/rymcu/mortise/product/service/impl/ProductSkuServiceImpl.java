package com.rymcu.mortise.product.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.mapper.ProductSkuMapper;
import com.rymcu.mortise.product.service.ProductSkuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.rymcu.mortise.product.entity.table.ProductSkuTableDef.PRODUCT_SKU;

/**
 * 产品 SKU 服务实现
 *
 * @author ronger
 */
@Service
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku>
        implements ProductSkuService {

    @Override
    public List<ProductSku> findByProductId(Long productId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PRODUCT_SKU.PRODUCT_ID.eq(productId))
                        .orderBy(PRODUCT_SKU.IS_DEFAULT.desc(), PRODUCT_SKU.CREATED_TIME.asc()));
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        ProductSku sku = UpdateEntity.of(ProductSku.class, id);
        sku.setStatus(status);
        sku.setUpdatedTime(LocalDateTime.now());
        return mapper.update(sku) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefault(Long productId, Long skuId) {
        // 先取消同产品全部 SKU 的默认标记
        ProductSku clearDefault = new ProductSku();
        clearDefault.setIsDefault(false);
        clearDefault.setUpdatedTime(LocalDateTime.now());
        mapper.updateByQuery(clearDefault,
                QueryWrapper.create().where(PRODUCT_SKU.PRODUCT_ID.eq(productId)));

        // 再将目标 SKU 设为默认
        ProductSku target = UpdateEntity.of(ProductSku.class, skuId);
        target.setIsDefault(true);
        target.setUpdatedTime(LocalDateTime.now());
        return mapper.update(target) > 0;
    }
}
