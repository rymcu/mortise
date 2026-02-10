package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.service.SystemCacheService;
import com.rymcu.mortise.web.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * 系统缓存管理控制器
 * 展示如何使用业务封装层 SystemCacheService
 *
 * @author ronger
 */
@Slf4j
@AdminController
@RequestMapping("/system/cache")
@Tag(name = "系统缓存管理", description = "系统缓存管理接口")
public class SystemCacheController {

    @Resource
    private SystemCacheService systemCacheService;

    /**
     * 清除用户缓存
     */
    @DeleteMapping("/user/{userId}")
    @Operation(summary = "清除用户缓存")
    @ApiLog("清除用户缓存")
    @OperationLog(module = "系统缓存", operation = "清除用户缓存")
    public GlobalResult<Void> evictUserCache(@PathVariable Long userId) {
        systemCacheService.evictUserAllCache(userId);
        return GlobalResult.success();
    }

    /**
     * 清除字典缓存
     */
    @DeleteMapping("/dict/{dictType}")
    @Operation(summary = "清除字典缓存")
    @ApiLog("清除字典缓存")
    @OperationLog(module = "系统缓存", operation = "清除字典缓存")
    public GlobalResult<Void> evictDictCache(@PathVariable String dictType) {
        systemCacheService.evictDictData(dictType);
        return GlobalResult.success();
    }

    /**
     * 清除所有字典缓存
     */
    @DeleteMapping("/dict/all")
    @Operation(summary = "清除所有字典缓存")
    @ApiLog("清除所有字典缓存")
    @OperationLog(module = "系统缓存", operation = "清除所有字典缓存")
    @RateLimit(name = "admin", message = "操作过于频繁")
    public GlobalResult<Void> evictAllDictCache() {
        systemCacheService.evictAllDictData();
        return GlobalResult.success();
    }

    /**
     * 发送验证码
     */
    @PostMapping("/verification-code")
    @Operation(summary = "发送验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "发送验证码")
    @OperationLog(module = "系统缓存", operation = "发送验证码", recordParams = false)
    @RateLimit(name = "verification-code", message = "验证码发送过于频繁，请稍后再试")
    public GlobalResult<Void> sendVerificationCode(@RequestParam String email) {
        // 生成验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

        // 缓存验证码（5分钟有效期）
        systemCacheService.cacheVerificationCode(email, code, Duration.ofMinutes(5));

        // TODO: 发送邮件（通过 SystemNotificationService）
        log.info("验证码已生成并缓存");

        return GlobalResult.success();
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verification-code/verify")
    @Operation(summary = "验证验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "验证验证码")
    public GlobalResult<Boolean> verifyCode(@RequestParam String email, @RequestParam String code) {
        String cachedCode = systemCacheService.getVerificationCode(email);

        if (cachedCode == null) {
            return GlobalResult.error("验证码已过期");
        }

        boolean isValid = cachedCode.equals(code);

        if (isValid) {
            // 验证成功后删除验证码
            systemCacheService.evictVerificationCode(email);
        }

        return GlobalResult.success(isValid);
    }
}

