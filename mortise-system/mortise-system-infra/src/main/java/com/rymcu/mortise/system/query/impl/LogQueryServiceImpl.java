package com.rymcu.mortise.system.query.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.persistence.log.entity.ApiLog;
import com.rymcu.mortise.persistence.log.entity.OperationLog;
import com.rymcu.mortise.persistence.log.mapper.ApiLogMapper;
import com.rymcu.mortise.persistence.log.mapper.OperationLogMapper;
import com.rymcu.mortise.system.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.system.model.LogSearch;
import com.rymcu.mortise.system.query.LogQueryService;
import com.rymcu.mortise.system.query.model.ApiLogView;
import com.rymcu.mortise.system.query.model.OperationLogView;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
/**
 * 日志读模型查询实现。
 */
@Service
public class LogQueryServiceImpl implements LogQueryService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final QueryColumn OP_OPERATOR_ACCOUNT = new QueryColumn("operator_account");
    private static final QueryColumn OP_REQUEST_URI = new QueryColumn("request_uri");
    private static final QueryColumn OP_OPERATION = new QueryColumn("operation");
    private static final QueryColumn OP_CLIENT_TYPE = new QueryColumn("client_type");
    private static final QueryColumn OP_MODULE = new QueryColumn("module");
    private static final QueryColumn OP_SUCCESS = new QueryColumn("success");
    private static final QueryColumn OP_OPERATE_TIME = new QueryColumn("operate_time");

    private static final QueryColumn API_USERNAME = new QueryColumn("username");
    private static final QueryColumn API_REQUEST_URI = new QueryColumn("request_uri");
    private static final QueryColumn API_DESCRIPTION = new QueryColumn("api_description");
    private static final QueryColumn API_CLIENT_TYPE = new QueryColumn("client_type");
    private static final QueryColumn API_SUCCESS = new QueryColumn("success");
    private static final QueryColumn API_REQUEST_TIME = new QueryColumn("request_time");

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private ApiLogMapper apiLogMapper;

    @Override
    public PageResult<OperationLogView> findOperationLogs(PageQuery pageQuery, LogSearch search) {
        QueryWrapper qw = QueryWrapper.create();
        if (StringUtils.isNotBlank(search.getQuery())) {
            String likeVal = search.getQuery();
            qw.and(OP_OPERATOR_ACCOUNT.like(likeVal)
                    .or(OP_REQUEST_URI.like(likeVal))
                    .or(OP_OPERATION.like(likeVal)));
        }
        qw.and(OP_CLIENT_TYPE.eq(search.getClientType(), StringUtils.isNotBlank(search.getClientType())));
        qw.and(OP_MODULE.eq(search.getModule(), StringUtils.isNotBlank(search.getModule())));
        qw.and(OP_SUCCESS.eq(search.getSuccess(), search.getSuccess() != null));
        if (StringUtils.isNotBlank(search.getStartDate())) {
            LocalDateTime start = LocalDate.parse(search.getStartDate(), DATE_FMT).atStartOfDay();
            qw.and(OP_OPERATE_TIME.ge(start));
        }
        if (StringUtils.isNotBlank(search.getEndDate())) {
            LocalDateTime end = LocalDate.parse(search.getEndDate(), DATE_FMT).atTime(LocalTime.MAX);
            qw.and(OP_OPERATE_TIME.le(end));
        }
        qw.orderBy(OP_OPERATE_TIME, false);
        Page<OperationLog> entityPage = operationLogMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), qw);
        return FlexPageMapper.toPageResult(entityPage, this::toOperationLogView);
    }

    @Override
    public Boolean deleteOperationLog(Long id) {
        return operationLogMapper.deleteById(id) > 0;
    }

    @Override
    public PageResult<ApiLogView> findApiLogs(PageQuery pageQuery, LogSearch search) {
        QueryWrapper qw = QueryWrapper.create();
        if (StringUtils.isNotBlank(search.getQuery())) {
            String likeVal = search.getQuery();
            qw.and(API_USERNAME.like(likeVal)
                    .or(API_REQUEST_URI.like(likeVal))
                    .or(API_DESCRIPTION.like(likeVal)));
        }
        qw.and(API_CLIENT_TYPE.eq(search.getClientType(), StringUtils.isNotBlank(search.getClientType())));
        qw.and(API_SUCCESS.eq(search.getSuccess(), search.getSuccess() != null));
        if (StringUtils.isNotBlank(search.getStartDate())) {
            LocalDateTime start = LocalDate.parse(search.getStartDate(), DATE_FMT).atStartOfDay();
            qw.and(API_REQUEST_TIME.ge(start));
        }
        if (StringUtils.isNotBlank(search.getEndDate())) {
            LocalDateTime end = LocalDate.parse(search.getEndDate(), DATE_FMT).atTime(LocalTime.MAX);
            qw.and(API_REQUEST_TIME.le(end));
        }
        qw.orderBy(API_REQUEST_TIME, false);
        Page<ApiLog> entityPage = apiLogMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), qw);
        return FlexPageMapper.toPageResult(entityPage, this::toApiLogView);
    }

    @Override
    public Boolean deleteApiLog(Long id) {
        return apiLogMapper.deleteById(id) > 0;
    }

    private OperationLogView toOperationLogView(OperationLog operationLog) {
        OperationLogView view = new OperationLogView();
        BeanUtils.copyProperties(operationLog, view);
        return view;
    }

    private ApiLogView toApiLogView(ApiLog apiLog) {
        ApiLogView view = new ApiLogView();
        BeanUtils.copyProperties(apiLog, view);
        return view;
    }
}
