package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.model.SystemInitInfo;
import com.rymcu.mortise.system.service.SystemInitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统初始化控制器
 *
 * @author ronger
 * @since 2025-10-02
 */
@Tag(name = "系统初始化", description = "系统初始化引导接口")
@RestController
@RequestMapping("/api/v1/system-init")
@RequiredArgsConstructor
public class SystemInitController {

    private final SystemInitService systemInitService;

    /**
     * 检查系统是否已初始化
     */
    @Operation(
        summary = "检查系统初始化状态",
        description = "检查系统是否已完成初始化配置，用于判断是否需要进入初始化引导流程"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功获取初始化状态",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\"initialized\": true}"
                )
            )
        )
    })
    @GetMapping("/status")
    @ApiLog("检查系统初始化状态")
    public GlobalResult<Map<String, Object>> checkInitStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("initialized", systemInitService.isSystemInitialized());
        return GlobalResult.success(result);
    }

    /**
     * 执行系统初始化
     */
    @Operation(
        summary = "执行系统初始化",
        description = "执行系统首次安装初始化，创建管理员账号和基础配置。系统只能初始化一次，初始化后此接口将返回错误。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "初始化执行完成（成功或失败都返回200，通过success字段判断）",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = {
                    @ExampleObject(
                        name = "成功示例",
                        value = "{\"success\": true, \"message\": \"系统初始化成功\"}"
                    ),
                    @ExampleObject(
                        name = "失败示例",
                        value = "{\"success\": false, \"message\": \"系统初始化失败\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "系统已初始化，无法重复初始化",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\"success\": false, \"message\": \"系统已经初始化，无法重复初始化\"}"
                )
            )
        )
    })
    @RequestBody(
        description = "系统初始化信息",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemInitInfo.class),
            examples = @ExampleObject(
                name = "初始化配置示例",
                value = """
                        {
                          "adminAccount": "admin",
                          "adminPassword": "Admin@123456",
                          "adminNickname": "系统管理员",
                          "adminEmail": "admin@example.com",
                          "systemName": "Mortise系统",
                          "systemDescription": "企业级管理系统"
                        }"""
            )
        )
    )
    @PostMapping("/initialize")
    @ApiLog(recordRequestBody = false, recordResponseBody = false, recordParams = false, value = "执行系统初始化")
    @OperationLog(module = "系统初始化", operation = "执行系统初始化", recordParams = false, recordResult = false)
    public GlobalResult<String> initializeSystem(@org.springframework.web.bind.annotation.RequestBody SystemInitInfo initInfo) {
        // 检查是否已初始化
        if (systemInitService.isSystemInitialized()) {
            return GlobalResult.error("系统已经初始化，无法重复初始化");
        }

        // 执行初始化
        boolean success = systemInitService.initializeSystem(initInfo);
        if (!success) {
            return GlobalResult.error("系统初始化失败");
        }
        return GlobalResult.success("系统初始化成功");
    }

    /**
     * 获取初始化进度
     */
    @Operation(
        summary = "获取初始化进度",
        description = "获取系统初始化的实时进度，返回0-100的百分比值。用于初始化过程中的进度展示。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功获取初始化进度",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = {
                    @ExampleObject(
                        name = "初始化中",
                        value = "{\"progress\": 45}"
                    ),
                    @ExampleObject(
                        name = "初始化完成",
                        value = "{\"progress\": 100}"
                    ),
                    @ExampleObject(
                        name = "未开始初始化",
                        value = "{\"progress\": 0}"
                    )
                }
            )
        )
    })
    @GetMapping("/progress")
    @ApiLog("获取初始化进度")
    public GlobalResult<Map<String, Object>> getInitProgress() {
        Map<String, Object> result = new HashMap<>();
        result.put("progress", systemInitService.getInitializationProgress());
        return GlobalResult.success(result);
    }
}
