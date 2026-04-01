package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.system.controller.vo.ApiLogVO;
import com.rymcu.mortise.system.controller.vo.OperationLogVO;
import com.rymcu.mortise.system.query.model.ApiLogView;
import com.rymcu.mortise.system.query.model.OperationLogView;

public final class LogAdminAssembler {

    private LogAdminAssembler() {
    }

    public static OperationLogVO toOperationLogVO(OperationLogView operationLogView) {
        if (operationLogView == null) {
            return null;
        }
        OperationLogVO operationLogVO = new OperationLogVO();
        operationLogVO.setId(operationLogView.getId());
        operationLogVO.setTraceId(operationLogView.getTraceId());
        operationLogVO.setClientType(operationLogView.getClientType());
        operationLogVO.setModule(operationLogView.getModule());
        operationLogVO.setOperation(operationLogView.getOperation());
        operationLogVO.setOperatorId(operationLogView.getOperatorId());
        operationLogVO.setOperatorAccount(operationLogView.getOperatorAccount());
        operationLogVO.setOperateTime(operationLogView.getOperateTime());
        operationLogVO.setMethod(operationLogView.getMethod());
        operationLogVO.setRequestUri(operationLogView.getRequestUri());
        operationLogVO.setRequestMethod(operationLogView.getRequestMethod());
        operationLogVO.setParams(operationLogView.getParams());
        operationLogVO.setResult(operationLogView.getResult());
        operationLogVO.setIp(operationLogView.getIp());
        operationLogVO.setUserAgent(operationLogView.getUserAgent());
        operationLogVO.setDuration(operationLogView.getDuration());
        operationLogVO.setSuccess(operationLogView.getSuccess());
        operationLogVO.setErrorMsg(operationLogView.getErrorMsg());
        operationLogVO.setCreatedTime(operationLogView.getCreatedTime());
        return operationLogVO;
    }

    public static ApiLogVO toApiLogVO(ApiLogView apiLogView) {
        if (apiLogView == null) {
            return null;
        }
        ApiLogVO apiLogVO = new ApiLogVO();
        apiLogVO.setId(apiLogView.getId());
        apiLogVO.setTraceId(apiLogView.getTraceId());
        apiLogVO.setClientType(apiLogView.getClientType());
        apiLogVO.setApiDescription(apiLogView.getApiDescription());
        apiLogVO.setClassName(apiLogView.getClassName());
        apiLogVO.setMethodName(apiLogView.getMethodName());
        apiLogVO.setUserId(apiLogView.getUserId());
        apiLogVO.setUsername(apiLogView.getUsername());
        apiLogVO.setRequestTime(apiLogView.getRequestTime());
        apiLogVO.setRequestUri(apiLogView.getRequestUri());
        apiLogVO.setRequestMethod(apiLogView.getRequestMethod());
        apiLogVO.setQueryString(apiLogView.getQueryString());
        apiLogVO.setRequestHeaders(apiLogView.getRequestHeaders());
        apiLogVO.setRequestBody(apiLogView.getRequestBody());
        apiLogVO.setResponseBody(apiLogView.getResponseBody());
        apiLogVO.setHttpStatus(apiLogView.getHttpStatus());
        apiLogVO.setClientIp(apiLogView.getClientIp());
        apiLogVO.setUserAgent(apiLogView.getUserAgent());
        apiLogVO.setDuration(apiLogView.getDuration());
        apiLogVO.setSuccess(apiLogView.getSuccess());
        apiLogVO.setErrorMsg(apiLogView.getErrorMsg());
        apiLogVO.setCreatedTime(apiLogView.getCreatedTime());
        return apiLogVO;
    }
}
