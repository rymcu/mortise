package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.DictTypeAdminFacade;
import com.rymcu.mortise.system.controller.request.DictTypeStatusRequest;
import com.rymcu.mortise.system.controller.request.DictTypeUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictTypeVO;
import com.rymcu.mortise.system.model.DictTypeSearch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 字典类型管理控制器
 * 提供字典类型的 CRUD 操作
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/9/22
 */
@Tag(name = "字典类型管理", description = "字典类型管理相关接口")
@AdminController
@RequestMapping("/dictionary-types")
public class DictTypeController {

    @Resource
    private DictTypeAdminFacade dictTypeAdminFacade;

    @Operation(summary = "获取字典类型列表", description = "分页查询字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('system:dict-type:list')")
    @ApiLog("查询字典类型列表")
    public GlobalResult<PageResult<DictTypeVO>> listDictType(@Parameter(description = "字典类型查询条件") @Valid DictTypeSearch search) {
        return GlobalResult.success(dictTypeAdminFacade.listDictTypes(search));
    }

    @Operation(summary = "获取字典类型详情", description = "根据ID获取字典类型详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict-type:query')")
    @ApiLog("获取字典类型详情")
    public GlobalResult<DictTypeVO> getDictTypeById(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType) {
        return GlobalResult.success(dictTypeAdminFacade.getDictTypeById(idDictType));
    }

    @Operation(summary = "创建字典类型", description = "新增字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('system:dict-type:add')")
    @ApiLog("创建字典类型")
    @OperationLog(module = "字典类型管理", operation = "创建字典类型", recordParams = true, recordResult = true)
    public GlobalResult<Long> createDictType(@Parameter(description = "字典类型信息", required = true) @Valid @RequestBody DictTypeUpsertRequest request,
                                                @AuthenticationPrincipal CurrentUser currentUser) {
        return GlobalResult.success(dictTypeAdminFacade.createDictType(request, currentUser));
    }

    @Operation(summary = "更新字典类型", description = "修改字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict-type:edit')")
    @ApiLog("更新字典类型")
    @OperationLog(module = "字典类型管理", operation = "更新字典类型", recordParams = true)
    public GlobalResult<Boolean> updateDictType(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType,
                                                @Parameter(description = "字典类型信息", required = true) @Valid @RequestBody DictTypeUpsertRequest request,
                                                @AuthenticationPrincipal CurrentUser currentUser) {
        return GlobalResult.success(dictTypeAdminFacade.updateDictType(idDictType, request, currentUser));
    }

    @Operation(summary = "更新字典类型状态", description = "启用/禁用字典类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:dict-type:edit')")
    @ApiLog("更新字典类型状态")
    @OperationLog(module = "字典类型管理", operation = "更新字典类型状态", recordParams = true)
    public GlobalResult<Boolean> updateDictTypeStatus(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType,
                                                      @Parameter(description = "字典类型状态信息", required = true) @Valid @RequestBody DictTypeStatusRequest request) {
        return GlobalResult.success(dictTypeAdminFacade.updateDictTypeStatus(idDictType, request));
    }

    @Operation(summary = "删除字典类型", description = "软删除字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict-type:delete')")
    @ApiLog("删除字典类型")
    @OperationLog(module = "字典类型管理", operation = "删除字典类型")
    public GlobalResult<Boolean> deleteDictType(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType) {
        return GlobalResult.success(dictTypeAdminFacade.deleteDictType(idDictType));
    }

    @Operation(summary = "批量删除字典类型", description = "批量软删除字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:dict-type:delete')")
    @ApiLog("批量删除字典类型")
    @OperationLog(module = "字典类型管理", operation = "批量删除字典类型", recordParams = true)
    public GlobalResult<Boolean> batchDeleteDictTypes(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(dictTypeAdminFacade.batchDeleteDictTypes(batchUpdateInfo));
    }

}
