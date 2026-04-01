package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.DictAdminFacade;
import com.rymcu.mortise.system.controller.request.DictStatusRequest;
import com.rymcu.mortise.system.controller.request.DictUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictVO;
import com.rymcu.mortise.system.model.DictSearch;
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
import java.util.List;

/**
 * 字典管理控制器
 * 提供字典数据的 CRUD 操作
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/9/22
 */
@Tag(name = "字典管理", description = "字典数据管理相关接口")
@AdminController
@RequestMapping("/dictionaries")
public class DictController {

    @Resource
    private DictAdminFacade dictAdminFacade;

    @Operation(summary = "获取字典列表", description = "分页查询字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('system:dict:list')")
    @ApiLog("查询字典列表")
    public GlobalResult<PageResult<DictVO>> listDict(@Parameter(description = "字典查询条件") @Valid DictSearch search) {
        return GlobalResult.success(dictAdminFacade.listDicts(search));
    }

    @Operation(summary = "获取字典详情", description = "根据ID获取字典详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @ApiLog("获取字典详情")
    public GlobalResult<DictVO> getDictById(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict) {
        return GlobalResult.success(dictAdminFacade.getDictById(idDict));
    }

    @Operation(summary = "创建字典", description = "新增字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('system:dict:add')")
    @ApiLog("创建字典")
    @OperationLog(module = "字典管理", operation = "创建字典", recordParams = true, recordResult = true)
    public GlobalResult<Long> createDict(@Parameter(description = "字典信息", required = true) @Valid @RequestBody DictUpsertRequest request,
                                            @AuthenticationPrincipal CurrentUser currentUser) {
        return GlobalResult.success(dictAdminFacade.createDict(request, currentUser));
    }

    @Operation(summary = "更新字典", description = "修改字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @ApiLog("更新字典")
    @OperationLog(module = "字典管理", operation = "更新字典", recordParams = true)
    public GlobalResult<Boolean> updateDict(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict,
                                            @Parameter(description = "字典信息", required = true) @Valid @RequestBody DictUpsertRequest request,
                                            @AuthenticationPrincipal CurrentUser currentUser) {
        return GlobalResult.success(dictAdminFacade.updateDict(idDict, request, currentUser));
    }

    @Operation(summary = "更新字典状态", description = "启用/禁用字典")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @ApiLog("更新字典状态")
    @OperationLog(module = "字典管理", operation = "更新字典状态", recordParams = true)
    public GlobalResult<Boolean> updateDictStatus(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict,
                                                  @Parameter(description = "字典状态信息", required = true) @Valid @RequestBody DictStatusRequest request) {
        return GlobalResult.success(dictAdminFacade.updateDictStatus(idDict, request));
    }

    @Operation(summary = "删除字典", description = "软删除字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @ApiLog("删除字典")
    @OperationLog(module = "字典管理", operation = "删除字典")
    public GlobalResult<Boolean> deleteDict(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict) {
        return GlobalResult.success(dictAdminFacade.deleteDict(idDict));
    }

    @Operation(summary = "获取字典选项", description = "根据字典类型编码获取选项列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @GetMapping("/options")
    @ApiLog("获取字典选项")
    public GlobalResult<List<BaseOption>> getDictOptions(@Parameter(description = "字典类型编码", required = true) @RequestParam("code") String dictTypeCode) {
        return GlobalResult.success(dictAdminFacade.getDictOptions(dictTypeCode));
    }

    @Operation(summary = "批量删除字典", description = "批量软删除字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @ApiLog("批量删除字典")
    @OperationLog(module = "字典管理", operation = "批量删除字典", recordParams = true)
    public GlobalResult<Boolean> batchDeleteDictionaries(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(dictAdminFacade.batchDeleteDictionaries(batchUpdateInfo));
    }

}
