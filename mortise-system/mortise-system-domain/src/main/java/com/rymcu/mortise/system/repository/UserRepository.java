package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户仓储端口。
 */
public interface UserRepository {

    boolean updateLastOnlineTimeByAccount(String account, LocalDateTime lastOnlineTime);

    boolean existsByNickname(String nickname);

    String findMaxAccount();

    List<User> findByIds(List<Long> userIds);

    User findById(Long userId);

    User findByAccount(String account);

    User findByLoginIdentity(String identity);

    boolean save(User user);

    boolean update(User user);

    boolean updateStatus(Long userId, Integer status);

    boolean updatePasswordById(Long userId, String encodedPassword);

    boolean updatePasswordByEmail(String email, String encodedPassword);

    boolean updateProfile(Long userId, String nickname, String avatar);

    boolean updateEmail(Long userId, String email);

    boolean updateLastLoginTimeByAccount(String account, LocalDateTime lastLoginTime, LocalDateTime lastOnlineTime);

    boolean deleteById(Long userId);

    boolean deleteByIds(List<Long> userIds);

    long count();

    long countEnabled();
}
