package com.rymcu.mortise.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.entity.DictType;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.enumerate.DelFlag;
import com.rymcu.mortise.model.DictSearch;
import com.rymcu.mortise.model.DictTypeSearch;
import com.rymcu.mortise.service.DictService;
import com.rymcu.mortise.service.DictTypeService;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;

/**
 * Created on 2024/9/22 20:21.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/admin/dict-type")
@PreAuthorize("hasRole('admin')")
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    @GetMapping("/list")
    public GlobalResult<IPage<DictType>> dictList(DictTypeSearch search) {
        Page<DictType> page = new Page<>(search.getPageNum(), search.getPageSize());
        IPage<DictType> list = dictTypeService.findDictTypeList(page, search);
        return GlobalResultGenerator.genSuccessResult(list);
    }

    @GetMapping("/detail/{idDict}")
    public GlobalResult<DictType> dictTypeDetail(@PathVariable Long idDictType) {
        DictType dictType = dictTypeService.findById(idDictType);
        return GlobalResultGenerator.genSuccessResult(dictType);
    }

    @PostMapping("/post")
    public GlobalResult<Boolean> addDictType(@RequestBody DictType dictType) throws AccountNotFoundException {
        User user = UserUtils.getCurrentUserByToken();
        dictType.setCreatedBy(user.getId());
        Boolean flag = dictTypeService.saveDictType(dictType);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PutMapping("/post")
    public GlobalResult<Boolean> updateDictType(@RequestBody DictType dictType) throws AccountNotFoundException {
        User user = UserUtils.getCurrentUserByToken();
        dictType.setUpdatedBy(user.getId());
        Boolean flag = dictTypeService.saveDictType(dictType);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PostMapping("/update-status")
    public GlobalResult<Boolean> updateStatus(@RequestBody DictType dictType) {
        return GlobalResultGenerator.genSuccessResult(dictTypeService.updateStatus(dictType.getId(), dictType.getStatus()));
    }

    @DeleteMapping("/update-del-flag")
    public GlobalResult<Boolean> updateDelFlag(Long idDictType) {
        return GlobalResultGenerator.genSuccessResult(dictTypeService.updateDelFlag(idDictType, DelFlag.DELETED.ordinal()));
    }

}
