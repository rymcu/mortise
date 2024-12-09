package com.rymcu.mortise.config;

import com.rymcu.mortise.core.constant.ProjectConstant;
import com.rymcu.mortise.util.Utils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2024/4/15 21:18.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.config
 */
@Configuration
public class JasyptEncryptionConfig {

    @Bean(name ="jasyptStringEncryptor")
    public StringEncryptor passwordEncryptor(){
        return Utils.initPasswordEncryptor(System.getenv(ProjectConstant.ENCRYPTION_KEY));
    }

}
