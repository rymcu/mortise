package com.rymcu.mortise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    int updateLastOnlineTimeByAccount(@Param("account") String account);

    User selectByAccount(@Param("account") String account);

    int updateLastLoginTime(@Param("account") String account);

    int insertUserRole(@Param("idUser") Long idUser, @Param("idRole") Long idRole);

    int selectCountByNickname(@Param("nickname") String nickname);

    String selectMaxAccount();

    List<UserInfo> selectUsers(@Param("page") Page<UserInfo> page, @Param("account") String account, @Param("email") String email, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("order") String order, @Param("sort") String sort, @Param("query") String query);

    int updatePasswordByEmail(@Param("email") String email, @Param("password") String password);

    UserInfo selectUserInfoById(@Param("idUser") Long idUser);

    int deleteUserRole(@Param("idUser") Long idUser);

    int updateStatus(@Param("idUser") Long idUser, @Param("status") Integer status);

    int updatePasswordById(@Param("idUser") Long idUser, @Param("password") String password);

    int updateDelFlag(@Param("idUser") Long idUser, @Param("delFlag") Integer delFlag);

    User selectByOpenId(@Param("provider") String provider, @Param("openId") String openId);

    Set<String> selectUserRolePermissionsByIdUser(@Param("idUser") Long idUser);
}
