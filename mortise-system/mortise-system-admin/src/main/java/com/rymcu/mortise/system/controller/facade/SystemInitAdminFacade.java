package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.model.SystemInitInfo;

import java.util.Map;

public interface SystemInitAdminFacade {

    GlobalResult<Map<String, Object>> checkInitStatus();

    GlobalResult<String> initializeSystem(SystemInitInfo initInfo);

    GlobalResult<Map<String, Object>> getInitProgress();
}
