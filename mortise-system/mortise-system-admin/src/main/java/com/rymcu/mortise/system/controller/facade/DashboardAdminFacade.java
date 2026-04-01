package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.model.DashboardStats;

public interface DashboardAdminFacade {

    GlobalResult<DashboardStats> getStats();
}
