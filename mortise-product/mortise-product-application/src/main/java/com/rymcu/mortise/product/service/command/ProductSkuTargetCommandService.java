package com.rymcu.mortise.product.service.command;

import com.rymcu.mortise.product.entity.ProductSkuTarget;

public interface ProductSkuTargetCommandService {

    boolean createTarget(ProductSkuTarget target);

    boolean updateTarget(ProductSkuTarget target);

    boolean deleteTarget(Long targetId);
}
