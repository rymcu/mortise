package com.rymcu.mortise.web;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.UserProfileInfo;
import com.rymcu.mortise.service.UserService;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2025/3/19 21:15.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserInfoController {

    @Resource
    private UserService userService;

    @PutMapping("/profile")
    public GlobalResult<Boolean> updateUserProfileInfo(@RequestBody UserProfileInfo userProfileInfo) {
        User user = UserUtils.getCurrentUserByToken();
        return GlobalResult.success(userService.updateUserProfileInfo(userProfileInfo, user));
    }
}
