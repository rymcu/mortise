package com.rymcu.mortise.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.enumerate.DelFlag;
import com.rymcu.mortise.model.DictSearch;
import com.rymcu.mortise.service.DictService;
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
@RequestMapping("/api/v1/admin/dict")
@PreAuthorize("hasRole('admin')")
public class DictController {

    @Resource
    private DictService dictService;

    @GetMapping("/list")
    public GlobalResult<IPage<Dict>> dictList(DictSearch search) {
        Page<Dict> page = new Page<>(search.getPageNum(), search.getPageSize());
        IPage<Dict> list = dictService.findDictList(page, search);
        return GlobalResultGenerator.genSuccessResult(list);
    }

    @GetMapping("/detail/{idDict}")
    public GlobalResult<Dict> dictDetail(@PathVariable Long idDict) {
        Dict dict = dictService.findById(idDict);
        return GlobalResultGenerator.genSuccessResult(dict);
    }

    @PostMapping("/post")
    public GlobalResult<Boolean> addDict(@RequestBody Dict dict) throws AccountNotFoundException {
        User user = UserUtils.getCurrentUserByToken();
        dict.setCreatedBy(user.getIdUser());
        Boolean flag = dictService.saveDict(dict);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PutMapping("/post")
    public GlobalResult<Boolean> updateDict(@RequestBody Dict dict) throws AccountNotFoundException {
        User user = UserUtils.getCurrentUserByToken();
        dict.setUpdatedBy(user.getIdUser());
        Boolean flag = dictService.saveDict(dict);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PostMapping("/update-status")
    public GlobalResult<Boolean> updateStatus(@RequestBody Dict dict) {
        return GlobalResultGenerator.genSuccessResult(dictService.updateStatus(dict.getIdDict(), dict.getStatus()));
    }

    @DeleteMapping("/update-del-flag")
    public GlobalResult<Boolean> updateDelFlag( Long idDict) {
        return GlobalResultGenerator.genSuccessResult(dictService.updateDelFlag(idDict, DelFlag.DELETE.ordinal()));
    }

}
