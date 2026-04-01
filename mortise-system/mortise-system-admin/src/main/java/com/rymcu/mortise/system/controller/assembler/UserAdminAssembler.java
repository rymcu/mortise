package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.common.model.Avatar;
import com.rymcu.mortise.system.controller.request.UserUpsertRequest;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.UserInfo;

import java.util.List;

/**
 * 管理端用户视图装配器。
 */
public final class UserAdminAssembler {

    private UserAdminAssembler() {
    }

    public static List<UserVO> toUserVOs(List<User> users) {
        return users.stream()
                .map(UserAdminAssembler::toUserVO)
                .toList();
    }

    public static UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setAccount(user.getAccount());
        userVO.setNickname(user.getNickname());
        userVO.setRealName(user.getRealName());
        userVO.setAvatar(toAvatar(user.getNickname(), user.getAvatar()));
        userVO.setPicture(user.getAvatar());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setStatus(user.getStatus());
        userVO.setDelFlag(user.getDelFlag());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setCreatedTime(user.getCreatedTime());
        userVO.setLastOnlineTime(user.getLastOnlineTime());
        return userVO;
    }

    public static List<UserVO> toUserInfoVOs(List<UserInfo> userInfos) {
        return userInfos.stream()
                .map(UserAdminAssembler::toUserVO)
                .toList();
    }

    public static UserVO toUserVO(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(userInfo.getId());
        userVO.setAccount(userInfo.getAccount());
        userVO.setNickname(userInfo.getNickname());
        userVO.setAvatar(userInfo.getAvatar());
        userVO.setPicture(userInfo.getPicture());
        userVO.setEmail(userInfo.getEmail());
        userVO.setPhone(userInfo.getPhone());
        userVO.setStatus(userInfo.getStatus());
        userVO.setLastLoginTime(userInfo.getLastLoginTime());
        userVO.setCreatedTime(userInfo.getCreatedTime());
        userVO.setLastOnlineTime(userInfo.getLastOnlineTime());
        userVO.setOnlineStatus(userInfo.getOnlineStatus());
        userVO.setRoleNames(userInfo.getRoleNames());
        userVO.setOpenId(userInfo.getOpenId());
        userVO.setProvider(userInfo.getProvider());
        return userVO;
    }

    public static UserInfo toUserInfo(UserUpsertRequest request) {
        UserInfo userInfo = new UserInfo();
        userInfo.setNickname(request.getNickname());
        userInfo.setEmail(request.getEmail());
        userInfo.setPassword(request.getPassword());
        userInfo.setPhone(request.getPhone());
        userInfo.setStatus(request.getStatus());
        userInfo.setPicture(request.getAvatar());
        userInfo.setAvatar(toAvatar(request.getNickname(), request.getAvatar()));
        return userInfo;
    }

    private static Avatar toAvatar(String alt, String picture) {
        if (picture == null) {
            return null;
        }
        Avatar avatar = new Avatar();
        avatar.setAlt(alt);
        avatar.setSrc(picture);
        return avatar;
    }
}
