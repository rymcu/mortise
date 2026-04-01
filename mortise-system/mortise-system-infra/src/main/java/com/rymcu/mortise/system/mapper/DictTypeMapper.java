package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.infra.persistence.entity.DictTypePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created on 2024/9/22 19:58.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictTypePO> {
}
