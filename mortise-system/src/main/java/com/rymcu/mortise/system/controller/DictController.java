package com.rymcu.mortise.system.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.DictSearch;
import com.rymcu.mortise.system.model.auth.UserDetailInfo;
import com.rymcu.mortise.system.service.DictService;
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
@RestController
@RequestMapping("/api/v1/admin/dictionaries")
@PreAuthorize("hasRole('admin')")
public class DictController {

    @Resource
    private DictService dictService;

    @Operation(summary = "获取字典列表", description = "分页查询字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    public GlobalResult<Page<Dict>> listDict(@Parameter(description = "字典查询条件") @Valid DictSearch search) {
        Page<Dict> page = new Page<>(search.getPageNum(), search.getPageSize());
        Page<Dict> result = dictService.findDictList(page, search);
        return GlobalResult.success(result);
    }

    @Operation(summary = "获取字典详情", description = "根据ID获取字典详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    public GlobalResult<Dict> getDictById(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict) {
        Dict dict = dictService.findById(idDict);
        return GlobalResult.success(dict);
    }

    @Operation(summary = "创建字典", description = "新增字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    public GlobalResult<Boolean> createDict(@Parameter(description = "字典信息", required = true) @Valid @RequestBody Dict dict,
                                            @AuthenticationPrincipal UserDetailInfo userDetails) {
        User user = userDetails.getUser();
        dict.setCreatedBy(user.getId());
        Boolean result = dictService.saveDict(dict);
        return GlobalResult.success(result);
    }

    @Operation(summary = "更新字典", description = "修改字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    public GlobalResult<Boolean> updateDict(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict,
                                            @Parameter(description = "字典信息", required = true) @Valid @RequestBody Dict dict,
                                            @AuthenticationPrincipal UserDetailInfo userDetails) {
        User user = userDetails.getUser();
        dict.setId(idDict);
        dict.setUpdatedBy(user.getId());
        Boolean result = dictService.saveDict(dict);
        return GlobalResult.success(result);
    }

    @Operation(summary = "更新字典状态", description = "启用/禁用字典")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    public GlobalResult<Boolean> updateDictStatus(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict,
                                                  @Parameter(description = "字典状态信息", required = true) @Valid @RequestBody Dict dict) {
        return GlobalResult.success(dictService.updateStatus(idDict, dict.getStatus()));
    }

    @Operation(summary = "删除字典", description = "软删除字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "字典不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    public GlobalResult<Boolean> deleteDict(@Parameter(description = "字典ID", required = true) @PathVariable("id") Long idDict) {
        return GlobalResult.success(dictService.deleteDict(idDict));
    }

    @Operation(summary = "获取字典选项", description = "根据字典类型编码获取选项列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @GetMapping("/options")
    public GlobalResult<List<BaseOption>> getDictOptions(@Parameter(description = "字典类型编码", required = true) @RequestParam("code") String dictTypeCode) {
        return GlobalResult.success(dictService.queryDictOptions(dictTypeCode));
    }

    @Operation(summary = "批量删除字典", description = "批量软删除字典数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    public GlobalResult<Boolean> batchDeleteDictionaries(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(dictService.batchDeleteDictionaries(batchUpdateInfo.getIds()));
    }

}
