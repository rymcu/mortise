package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.SystemCacheAdminFacade;
import com.rymcu.mortise.web.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统缓存管理控制器
 * 展示如何使用业务封装层 SystemCacheService
 *
 * @author ronger
 */
@AdminController
@RequestMapping("/system/cache")
@Tag(name = "系统缓存管理", description = "系统缓存管理接口")
public class SystemCacheController {

    @Resource
    private SystemCacheAdminFacade systemCacheAdminFacade;

    /**
     * 清除用户缓存
     */
    @DeleteMapping("/user/{userId}")
    @Operation(summary = "清除用户缓存")
    @PreAuthorize("hasAuthority('system:cache:delete')")
    @ApiLog("清除用户缓存")
    @OperationLog(module = "系统缓存", operation = "清除用户缓存")
    public GlobalResult<Void> evictUserCache(@PathVariable Long userId) {
        return systemCacheAdminFacade.evictUserCache(userId);
    }

    /**
     * 清除字典缓存
     */
    @DeleteMapping("/dict/{dictType}")
    @Operation(summary = "清除字典缓存")
    @PreAuthorize("hasAuthority('system:cache:delete')")
    @ApiLog("清除字典缓存")
    @OperationLog(module = "系统缓存", operation = "清除字典缓存")
    public GlobalResult<Void> evictDictCache(@PathVariable String dictType) {
        return systemCacheAdminFacade.evictDictCache(dictType);
    }

    /**
     * 清除所有字典缓存
     */
    @DeleteMapping("/dict/all")
    @Operation(summary = "清除所有字典缓存")
    @PreAuthorize("hasAuthority('system:cache:clear')")
    @ApiLog("清除所有字典缓存")
    @OperationLog(module = "系统缓存", operation = "清除所有字典缓存")
    @RateLimit(name = "admin", message = "操作过于频繁")
    public GlobalResult<Void> evictAllDictCache() {
        return systemCacheAdminFacade.evictAllDictCache();
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
        return systemCacheAdminFacade.sendVerificationCode(email);
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verification-code/verify")
    @Operation(summary = "验证验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "验证验证码")
    public GlobalResult<Boolean> verifyCode(@RequestParam String email, @RequestParam String code) {
        return systemCacheAdminFacade.verifyCode(email, code);
    }
}
