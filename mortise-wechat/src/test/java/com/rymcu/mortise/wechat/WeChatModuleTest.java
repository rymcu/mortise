package com.rymcu.mortise.wechat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

/**
 * 微信模块测试基类
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest
public class WeChatModuleTest {

    @Test
    public void contextLoads() {
        log.info("微信模块测试上下文加载成功");
    }
}
