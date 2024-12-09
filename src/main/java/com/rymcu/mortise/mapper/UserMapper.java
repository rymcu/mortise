package com.rymcu.mortise.mapper;

import com.rymcu.mortise.core.mapper.Mapper;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.UserInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2024/4/13 15:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
public interface UserMapper extends Mapper<User> {
    int updateLastOnlineTimeByAccount(@Param("account") String account);

    User selectByAccount(@Param("account") String account);

    int updateLastLoginTime(@Param("idUser") Long idUser);

    int insertUserRole(@Param("idUser") Long idUser, @Param("idRole") Long idRole);

    int selectCountByNickname(@Param("nickname") String nickname);

    String selectMaxAccount();

    List<UserInfo> selectUsers(@Param("account") String account, @Param("email") String email, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("order") String order, @Param("sort") String sort, @Param("query") String query);

    int updatePasswordByEmail(@Param("email") String email, @Param("password") String password);

    UserInfo selectUserInfoById(@Param("idUser") Long idUser);

    int deleteUserRole(@Param("idUser") Long idUser);

    int updateStatus(@Param("idUser") Long idUser, @Param("status") Integer status);

    int updatePasswordById(@Param("idUser") Long idUser, @Param("password") String password);

    int updateDelFlag(@Param("idUser") Long idUser, @Param("delFlag") Integer delFlag);
}
