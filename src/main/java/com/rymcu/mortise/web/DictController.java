package com.rymcu.mortise.web;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.enumerate.DelFlag;
import com.rymcu.mortise.model.BaseOption;
import com.rymcu.mortise.model.BatchUpdateInfo;
import com.rymcu.mortise.model.DictSearch;
import com.rymcu.mortise.service.DictService;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created on 2024/9/22 20:21.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/admin/dict")
public class DictController {

    @Resource
    private DictService dictService;

    @GetMapping("/list")
    public GlobalResult<Page<Dict>> dictList(DictSearch search) {
        Page<Dict> page = new Page<>(search.getPageNum(), search.getPageSize());
        Page<Dict> list = dictService.findDictList(page, search);
        return GlobalResult.success(list);
    }

    @GetMapping("/detail/{idDict}")
    public GlobalResult<Dict> dictDetail(@PathVariable Long idDict) {
        Dict dict = dictService.findById(idDict);
        return GlobalResult.success(dict);
    }

    @PostMapping("/post")
    public GlobalResult<Boolean> addDict(@RequestBody Dict dict) {
        User user = UserUtils.getCurrentUserByToken();
        dict.setCreatedBy(user.getId());
        Boolean flag = dictService.saveDict(dict);
        return GlobalResult.success(flag);
    }

    @PutMapping("/post")
    public GlobalResult<Boolean> updateDict(@RequestBody Dict dict) {
        User user = UserUtils.getCurrentUserByToken();
        dict.setUpdatedBy(user.getId());
        Boolean flag = dictService.saveDict(dict);
        return GlobalResult.success(flag);
    }

    @PatchMapping("/update-status")
    public GlobalResult<Boolean> updateStatus(@RequestBody Dict dict) {
        return GlobalResult.success(dictService.updateStatus(dict.getId(), dict.getStatus()));
    }

    @PatchMapping("/update-del-flag")
    public GlobalResult<Boolean> updateDelFlag(Long idDict) {
        return GlobalResult.success(dictService.updateDelFlag(idDict, DelFlag.DELETED.ordinal()));
    }

    @GetMapping("/options")
    public GlobalResult<List<BaseOption>> queryDictOptions(@RequestParam("code") String dictTypeCode) {
        return GlobalResult.success(dictService.queryDictOptions(dictTypeCode));
    }

    @PatchMapping("/batch-update-del-flag")
    public GlobalResult<Boolean> batchUpdateDelFlag(@RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(dictService.batchUpdateDelFlag(batchUpdateInfo.getIds(), DelFlag.DELETED.ordinal()));
    }

}
