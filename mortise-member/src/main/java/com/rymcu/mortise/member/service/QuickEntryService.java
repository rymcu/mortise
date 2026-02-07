package com.rymcu.mortise.member.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.QuickEntry;
import com.rymcu.mortise.member.model.QuickEntrySearch;

import java.util.List;
import java.util.Map;

/**
 * 金刚区快捷入口服务接口
 *
 * @author ronger
 */
public interface QuickEntryService extends IService<QuickEntry> {

    /**
     * 分页查询快捷入口
     *
     * @param page   分页对象
     * @param search 查询条件
     * @return 分页数据
     */
    Page<QuickEntry> findEntryList(Page<QuickEntry> page, QuickEntrySearch search);

    /**
     * 获取有效的快捷入口列表（前端展示用）
     *
     * @param position 展示位置
     * @param platform 平台
     * @return 快捷入口列表
     */
    List<QuickEntry> findActiveEntries(String position, String platform);

    /**
     * 按行分组获取有效的快捷入口（前端展示用）
     *
     * @param position 展示位置
     * @param platform 平台
     * @return 按行索引分组的快捷入口
     */
    Map<Integer, List<QuickEntry>> findActiveEntriesGroupByRow(String position, String platform);

    /**
     * 增加点击次数
     *
     * @param id 快捷入口ID
     * @return 是否成功
     */
    Boolean incrementClickCount(Long id);

    /**
     * 启用快捷入口
     *
     * @param id 快捷入口ID
     * @return 是否成功
     */
    Boolean enable(Long id);

    /**
     * 禁用快捷入口
     *
     * @param id 快捷入口ID
     * @return 是否成功
     */
    Boolean disable(Long id);

    /**
     * 更新排序
     *
     * @param id     快捷入口ID
     * @param sortNo 新的排序号
     * @return 是否成功
     */
    Boolean updateSort(Long id, Integer sortNo);

    /**
     * 更新行索引
     *
     * @param id       快捷入口ID
     * @param rowIndex 新的行索引
     * @return 是否成功
     */
    Boolean updateRowIndex(Long id, Integer rowIndex);
}
