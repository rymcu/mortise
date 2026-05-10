package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.MemberClientSession;

import java.util.List;

/**
 * 会员客户端会话服务。
 *
 * @author ronger
 */
public interface MemberClientSessionService extends IService<MemberClientSession> {

    /**
     * 创建桌面客户端会话。
     *
     * @param memberId              会员ID
     * @param clientId              客户端ID
     * @param deviceName            设备名称
     * @param deviceFingerprintHash 设备指纹哈希
     * @return 会话
     */
    MemberClientSession createSession(Long memberId,
                                      String clientId,
                                      String deviceName,
                                      String deviceFingerprintHash);

    /**
     * 标记会话活跃。
     *
     * @param sessionId 会话ID
     * @return 会话，不存在或已撤销时返回 null
     */
    MemberClientSession touchActiveSession(Long sessionId);

    /**
     * 查询会员客户端会话。
     *
     * @param memberId 会员ID
     * @return 会话列表
     */
    List<MemberClientSession> listMemberSessions(Long memberId);

    /**
     * 撤销会话。
     *
     * @param sessionId 会话ID
     * @return 是否撤销成功
     */
    Boolean revokeSession(Long sessionId);

    /**
     * 撤销会员自己的会话。
     *
     * @param memberId 会员ID
     * @param sessionId 会话ID
     * @return 是否撤销成功
     */
    Boolean revokeMemberSession(Long memberId, Long sessionId);
}
