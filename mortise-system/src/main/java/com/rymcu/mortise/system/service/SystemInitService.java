package com.rymcu.mortise.system.service;

import com.rymcu.mortise.system.model.SystemInitInfo;

/**
 * 系统初始化服务接口
 * 
 * @author ronger
 * @since 2025-10-02
 */
public interface SystemInitService {

    /**
     * 检查系统是否已初始化
     * 
     * @return true-已初始化，false-未初始化
     */
    boolean isSystemInitialized();

    /**
     * 执行系统初始化
     * 
     * @param initInfo 初始化信息（包括管理员账号、密码等）
     * @return 初始化结果
     */
    boolean initializeSystem(SystemInitInfo initInfo);

    /**
     * 获取初始化进度
     * 
     * @return 初始化进度百分比 (0-100)
     */
    int getInitializationProgress();
}
