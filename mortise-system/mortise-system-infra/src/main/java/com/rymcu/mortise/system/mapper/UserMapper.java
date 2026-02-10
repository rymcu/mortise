package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created on 2024/4/13 15:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
