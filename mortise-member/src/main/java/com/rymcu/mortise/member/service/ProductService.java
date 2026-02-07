package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.Product;
import com.rymcu.mortise.member.model.ProductCourseOutlineInfo;

/**
 * 商品基础服务
 * <p>
 * 提供商品的基础 CRUD 操作
 * <p>
 * 业务场景说明：
 * - 管理端（mortise-edu）：需要商品的完整管理功能（CRUD、上下架、分类管理等）
 * - 客户端（mortise-api）：仅需要商品查询、浏览等只读功能
 * <p>
 * 设计原则：
 * - ✅ 本接口仅提供最基础的 CRUD（继承自 IService）
 * - ✅ 特定业务场景的方法应在各业务模块中扩展实现
 * - ❌ 不在此接口中定义特定场景的业务方法
 * <p>
 * 扩展示例：
 * <pre>
 * // mortise-api 中扩展（客户端仅需查询）
 * public interface ApiProductService extends ProductService {
 *     Page&lt;Product&gt; findPublishedProducts(Page&lt;Product&gt; page, ProductSearch search);
 *     Product getProductDetail(Long productId);
 *     Boolean incrementViewCount(Long productId);
 * }
 *
 * // mortise-edu 中扩展（管理端需要完整管理功能）
 * public interface EduProductService extends ProductService {
 *     Page&lt;Product&gt; findProductList(Page&lt;Product&gt; page, ProductSearch search);
 *     Long createProduct(Product product);
 *     Boolean updateProduct(Product product);
 *     Boolean publishProduct(Long productId);
 *     Boolean onSale(Long productId);
 *     Boolean offSale(Long productId);
 * }
 * </pre>
 *
 * @author ronger
 */
public interface ProductService extends IService<Product> {
    /**
     * 增加浏览次数
     *
     * @param id 商品ID
     * @return 是否成功
     */
    Boolean incrementViewCount(Long id);

    /**
     * 获取产品的课程大纲
     * 通过 ProductSkuTarget 关联获取所有相关课程的模块和课时信息
     *
     * @param productId 产品ID
     * @return 课程大纲信息
     */
    ProductCourseOutlineInfo getProductCourseOutline(Long productId);

    /**
     * 获取产品的课程大纲（指定SKU）
     *
     * @param productId 产品ID
     * @param skuId SKU ID
     * @return 课程大纲信息
     */
    ProductCourseOutlineInfo getProductCourseOutlineBySku(Long productId, Long skuId);

}
