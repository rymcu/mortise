package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.SystemInitAdminFacade;
import com.rymcu.mortise.system.model.SystemInitInfo;
import com.rymcu.mortise.system.service.SystemInitService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SystemInitAdminFacadeImpl implements SystemInitAdminFacade {

    private final SystemInitService systemInitService;

    public SystemInitAdminFacadeImpl(SystemInitService systemInitService) {
        this.systemInitService = systemInitService;
    }

    @Override
    public GlobalResult<Map<String, Object>> checkInitStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("initialized", systemInitService.isSystemInitialized());
        return GlobalResult.success(result);
    }

    @Override
    public GlobalResult<String> initializeSystem(SystemInitInfo initInfo) {
        if (systemInitService.isSystemInitialized()) {
            return GlobalResult.error("系统已经初始化，无法重复初始化");
        }

        boolean success = systemInitService.initializeSystem(initInfo);
        if (!success) {
            return GlobalResult.error("系统初始化失败");
        }
        return GlobalResult.success("系统初始化成功");
    }

    @Override
    public GlobalResult<Map<String, Object>> getInitProgress() {
        Map<String, Object> result = new HashMap<>();
        result.put("progress", systemInitService.getInitializationProgress());
        return GlobalResult.success(result);
    }
}
