package com.rymcu.mortise.core.config;

import com.rymcu.mortise.common.constant.ProjectConstant;
import com.rymcu.mortise.core.util.JasyptUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt 加密配置
 * <p>
 * 用于加密 application.yml 中的敏感信息（如数据库密码、邮箱密码等）
 * 使用方式：ENC(加密后的字符串)
 *
 * @author ronger
 */
@Configuration
public class JasyptEncryptionConfig {

    /**
     * 配置 Jasypt 加密器
     * <p>
     * 从环境变量中读取加密密钥：ENCRYPTION_KEY
     *
     * @return StringEncryptor
     */
    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor passwordEncryptor() {
        return JasyptUtils.initPasswordEncryptor(System.getenv(ProjectConstant.ENCRYPTION_KEY));
    }
}
