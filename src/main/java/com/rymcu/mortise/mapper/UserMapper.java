package com.rymcu.mortise.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created on 2024/4/13 15:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByAccount(@Param("account") String account);

    int insertUserRole(@Param("idUser") Long idUser, @Param("idRole") Long idRole);

    List<UserInfo> selectUsers(@Param("page") Page<UserInfo> page, @Param("account") String account, @Param("email") String email, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("order") String order, @Param("sort") String sort, @Param("query") String query);

    long selectUsers_COUNT(@Param("account") String account, @Param("email") String email, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("order") String order, @Param("sort") String sort, @Param("query") String query);

    int deleteUserRole(@Param("idUser") Long idUser);

    Set<String> selectUserRolePermissionsByIdUser(@Param("idUser") Long idUser);

}
