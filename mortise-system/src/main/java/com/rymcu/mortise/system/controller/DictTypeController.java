package com.rymcu.mortise.system.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.DictTypeSearch;
import com.rymcu.mortise.system.model.auth.UserDetailInfo;
import com.rymcu.mortise.system.service.DictTypeService;
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
@RestController
@RequestMapping("/api/v1/admin/dictionary-types")
@PreAuthorize("hasRole('ADMIN')")
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    @Operation(summary = "获取字典类型列表", description = "分页查询字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    public GlobalResult<Page<DictType>> listDictType(@Parameter(description = "字典类型查询条件") @Valid DictTypeSearch search) {
        Page<DictType> page = new Page<>(search.getPageNum(), search.getPageSize());
        return GlobalResult.success(dictTypeService.findDictTypeList(page, search));
    }

    @Operation(summary = "获取字典类型详情", description = "根据ID获取字典类型详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    public GlobalResult<DictType> getDictTypeById(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType) {
        DictType dictType = dictTypeService.findById(idDictType);
        return GlobalResult.success(dictType);
    }

    @Operation(summary = "创建字典类型", description = "新增字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    public GlobalResult<Boolean> createDictType(@Parameter(description = "字典类型信息", required = true) @Valid @RequestBody DictType dictType,
                                                @AuthenticationPrincipal UserDetailInfo userDetails) {
        User user = userDetails.getUser();
        dictType.setCreatedBy(user.getId());
        Boolean result = dictTypeService.saveDictType(dictType);
        return GlobalResult.success(result);
    }

    @Operation(summary = "更新字典类型", description = "修改字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    public GlobalResult<Boolean> updateDictType(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType,
                                                @Parameter(description = "字典类型信息", required = true) @Valid @RequestBody DictType dictType,
                                                @AuthenticationPrincipal UserDetailInfo userDetails) {
        User user = userDetails.getUser();
        dictType.setId(idDictType);
        dictType.setUpdatedBy(user.getId());
        Boolean result = dictTypeService.saveDictType(dictType);
        return GlobalResult.success(result);
    }

    @Operation(summary = "更新字典类型状态", description = "启用/禁用字典类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    public GlobalResult<Boolean> updateDictTypeStatus(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType,
                                                      @Parameter(description = "字典类型状态信息", required = true) @Valid @RequestBody DictType dictType) {
        return GlobalResult.success(dictTypeService.updateStatus(idDictType, dictType.getStatus()));
    }

    @Operation(summary = "删除字典类型", description = "软删除字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "字典类型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    public GlobalResult<Boolean> deleteDictType(@Parameter(description = "字典类型ID", required = true) @PathVariable("id") Long idDictType) {
        return GlobalResult.success(dictTypeService.deleteDictType(idDictType));
    }

    @Operation(summary = "批量删除字典类型", description = "批量软删除字典类型数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    public GlobalResult<Boolean> batchDeleteDictTypes(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(dictTypeService.batchDeleteDictTypes(batchUpdateInfo.getIds()));
    }

}
