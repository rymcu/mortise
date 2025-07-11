package com.rymcu.mortise.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.DictType;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.enumerate.DelFlag;
import com.rymcu.mortise.model.BatchUpdateInfo;
import com.rymcu.mortise.model.DictTypeSearch;
import com.rymcu.mortise.service.DictTypeService;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * Created on 2024/9/22 20:21.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/admin/dict-type")
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    @GetMapping("/list")
    public GlobalResult<IPage<DictType>> dictList(DictTypeSearch search) {
        Page<DictType> page = new Page<>(search.getPageNum(), search.getPageSize());
        IPage<DictType> list = dictTypeService.findDictTypeList(page, search);
        return GlobalResult.success(list);
    }

    @GetMapping("/detail/{idDictType}")
    public GlobalResult<DictType> dictTypeDetail(@PathVariable Long idDictType) {
        DictType dictType = dictTypeService.findById(idDictType);
        return GlobalResult.success(dictType);
    }

    @PostMapping("/post")
    public GlobalResult<Boolean> addDictType(@RequestBody DictType dictType) {
        User user = UserUtils.getCurrentUserByToken();
        dictType.setCreatedBy(user.getId());
        Boolean flag = dictTypeService.saveDictType(dictType);
        return GlobalResult.success(flag);
    }

    @PutMapping("/post")
    public GlobalResult<Boolean> updateDictType(@RequestBody DictType dictType) {
        User user = UserUtils.getCurrentUserByToken();
        dictType.setUpdatedBy(user.getId());
        Boolean flag = dictTypeService.saveDictType(dictType);
        return GlobalResult.success(flag);
    }

    @PatchMapping("/update-status")
    public GlobalResult<Boolean> updateStatus(@RequestBody DictType dictType) {
        return GlobalResult.success(dictTypeService.updateStatus(dictType.getId(), dictType.getStatus()));
    }

    @PatchMapping("/update-del-flag")
    public GlobalResult<Boolean> updateDelFlag(Long idDictType) {
        return GlobalResult.success(dictTypeService.updateDelFlag(idDictType, DelFlag.DELETED.ordinal()));
    }

    @PatchMapping("/batch-update-del-flag")
    public GlobalResult<Boolean> batchUpdateDelFlag(@RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(dictTypeService.batchUpdateDelFlag(batchUpdateInfo.getIds(), DelFlag.DELETED.ordinal()));
    }

}
