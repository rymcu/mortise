package com.rymcu.mortise.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二维码生成结果
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResult {

    /**
     * 二维码 ticket（凭证），可用于换取二维码图片
     */
    private String ticket;

    /**
     * 二维码图片解析后的地址
     * <p>
     * 开发者可根据该地址自行生成需要的二维码图片
     */
    private String url;

    /**
     * 二维码有效时间（秒）
     * <p>
     * 最大不超过 2592000（即30天）
     */
    private Integer expireSeconds;

    /**
     * 场景值（用于标识登录会话）
     */
    private String sceneStr;
}
