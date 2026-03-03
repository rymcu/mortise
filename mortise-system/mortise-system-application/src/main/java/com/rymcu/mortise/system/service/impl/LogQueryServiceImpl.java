package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.persistence.log.entity.ApiLog;
import com.rymcu.mortise.persistence.log.entity.OperationLog;
import com.rymcu.mortise.persistence.log.mapper.ApiLogMapper;
import com.rymcu.mortise.persistence.log.mapper.OperationLogMapper;
import com.rymcu.mortise.system.model.LogSearch;
import com.rymcu.mortise.system.service.LogQueryService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志查询服务实现
 * 提供操作日志和 API 日志的分页查询与删除功能
 *
 * @author ronger
 */
@Service
public class LogQueryServiceImpl implements LogQueryService {

    /** 日期格式 */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // -------- 操作日志列名常量 --------
    private static final QueryColumn OP_OPERATOR_ACCOUNT = new QueryColumn("operator_account");
    private static final QueryColumn OP_REQUEST_URI      = new QueryColumn("request_uri");
    private static final QueryColumn OP_OPERATION        = new QueryColumn("operation");
    private static final QueryColumn OP_CLIENT_TYPE      = new QueryColumn("client_type");
    private static final QueryColumn OP_MODULE           = new QueryColumn("module");
    private static final QueryColumn OP_SUCCESS          = new QueryColumn("success");
    private static final QueryColumn OP_OPERATE_TIME     = new QueryColumn("operate_time");

    // -------- API 日志列名常量 --------
    private static final QueryColumn API_USERNAME        = new QueryColumn("username");
    private static final QueryColumn API_REQUEST_URI     = new QueryColumn("request_uri");
    private static final QueryColumn API_DESCRIPTION     = new QueryColumn("api_description");
    private static final QueryColumn API_CLIENT_TYPE     = new QueryColumn("client_type");
    private static final QueryColumn API_SUCCESS         = new QueryColumn("success");
    private static final QueryColumn API_REQUEST_TIME    = new QueryColumn("request_time");

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private ApiLogMapper apiLogMapper;

    @Override
    public Page<OperationLog> findOperationLogs(Page<OperationLog> page, LogSearch search) {
        QueryWrapper qw = QueryWrapper.create();

        // 关键词模糊匹配：操作人 / URI / 操作类型
        if (StringUtils.isNotBlank(search.getQuery())) {
            String likeVal = search.getQuery();
            qw.and(
                OP_OPERATOR_ACCOUNT.like(likeVal)
                    .or(OP_REQUEST_URI.like(likeVal))
                    .or(OP_OPERATION.like(likeVal))
            );
        }

        // 精确过滤条件（条件内联到 QueryCondition 的第二个参数中）
        qw.and(OP_CLIENT_TYPE.eq(search.getClientType(), StringUtils.isNotBlank(search.getClientType())));
        qw.and(OP_MODULE.eq(search.getModule(),           StringUtils.isNotBlank(search.getModule())));
        qw.and(OP_SUCCESS.eq(search.getSuccess(),         search.getSuccess() != null));

        // 时间范围
        if (StringUtils.isNotBlank(search.getStartDate())) {
            LocalDateTime start = LocalDate.parse(search.getStartDate(), DATE_FMT).atStartOfDay();
            qw.and(OP_OPERATE_TIME.ge(start));
        }
        if (StringUtils.isNotBlank(search.getEndDate())) {
            LocalDateTime end = LocalDate.parse(search.getEndDate(), DATE_FMT).atTime(LocalTime.MAX);
            qw.and(OP_OPERATE_TIME.le(end));
        }

        qw.orderBy(OP_OPERATE_TIME, false);

        return operationLogMapper.paginate(page, qw);
    }

    @Override
    public Boolean deleteOperationLog(Long id) {
        return operationLogMapper.deleteById(id) > 0;
    }

    @Override
    public Page<ApiLog> findApiLogs(Page<ApiLog> page, LogSearch search) {
        QueryWrapper qw = QueryWrapper.create();

        // 关键词模糊匹配：用户名 / URI / API 描述
        if (StringUtils.isNotBlank(search.getQuery())) {
            String likeVal = search.getQuery();
            qw.and(
                API_USERNAME.like(likeVal)
                    .or(API_REQUEST_URI.like(likeVal))
                    .or(API_DESCRIPTION.like(likeVal))
            );
        }

        // 精确过滤条件（条件内联到 QueryCondition 的第二个参数中）
        qw.and(API_CLIENT_TYPE.eq(search.getClientType(), StringUtils.isNotBlank(search.getClientType())));
        qw.and(API_SUCCESS.eq(search.getSuccess(),        search.getSuccess() != null));

        // 时间范围
        if (StringUtils.isNotBlank(search.getStartDate())) {
            LocalDateTime start = LocalDate.parse(search.getStartDate(), DATE_FMT).atStartOfDay();
            qw.and(API_REQUEST_TIME.ge(start));
        }
        if (StringUtils.isNotBlank(search.getEndDate())) {
            LocalDateTime end = LocalDate.parse(search.getEndDate(), DATE_FMT).atTime(LocalTime.MAX);
            qw.and(API_REQUEST_TIME.le(end));
        }

        qw.orderBy(API_REQUEST_TIME, false);

        return apiLogMapper.paginate(page, qw);
    }

    @Override
    public Boolean deleteApiLog(Long id) {
        return apiLogMapper.deleteById(id) > 0;
    }
}
