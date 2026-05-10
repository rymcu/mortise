package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.member.constant.DesktopOAuthConstants;
import com.rymcu.mortise.member.entity.MemberClientSession;
import com.rymcu.mortise.member.mapper.MemberClientSessionMapper;
import com.rymcu.mortise.member.service.MemberClientSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.rymcu.mortise.member.entity.table.MemberClientSessionTableDef.MEMBER_CLIENT_SESSION;

/**
 * 会员客户端会话服务实现。
 *
 * @author ronger
 */
@Slf4j
@Service
public class MemberClientSessionServiceImpl
        extends ServiceImpl<MemberClientSessionMapper, MemberClientSession>
        implements MemberClientSessionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberClientSession createSession(Long memberId,
                                             String clientId,
                                             String deviceName,
                                             String deviceFingerprintHash) {
        LocalDateTime now = LocalDateTime.now();
        MemberClientSession session = new MemberClientSession();
        session.setMemberId(memberId);
        session.setClientId(clientId);
        session.setDeviceName(deviceName);
        session.setDeviceFingerprintHash(deviceFingerprintHash);
        session.setStatus(DesktopOAuthConstants.SESSION_STATUS_ACTIVE);
        session.setLastActiveAt(now);
        session.setCreatedTime(now);
        session.setUpdatedTime(now);
        session.setDelFlag(DelFlag.NORMAL.ordinal());
        save(session);
        log.info("创建会员客户端会话: sessionId={}, memberId={}, clientId={}",
                session.getId(), memberId, clientId);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberClientSession touchActiveSession(Long sessionId) {
        if (sessionId == null) {
            return null;
        }

        MemberClientSession session = getById(sessionId);
        if (session == null
                || !DesktopOAuthConstants.SESSION_STATUS_ACTIVE.equals(session.getStatus())) {
            return null;
        }

        session.setLastActiveAt(LocalDateTime.now());
        session.setUpdatedTime(LocalDateTime.now());
        updateById(session);
        return session;
    }

    @Override
    public List<MemberClientSession> listMemberSessions(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }

        return list(QueryWrapper.create()
                .where(MEMBER_CLIENT_SESSION.MEMBER_ID.eq(memberId))
                .and(MEMBER_CLIENT_SESSION.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                .orderBy(MEMBER_CLIENT_SESSION.LAST_ACTIVE_AT.desc(), MEMBER_CLIENT_SESSION.CREATED_TIME.desc()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeSession(Long sessionId) {
        if (sessionId == null) {
            return false;
        }

        MemberClientSession session = getById(sessionId);
        if (session == null) {
            return false;
        }

        session.setStatus(DesktopOAuthConstants.SESSION_STATUS_REVOKED);
        session.setRevokedAt(LocalDateTime.now());
        session.setUpdatedTime(LocalDateTime.now());
        boolean updated = updateById(session);
        if (updated) {
            log.info("撤销会员客户端会话: sessionId={}, memberId={}, clientId={}",
                    session.getId(), session.getMemberId(), session.getClientId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeMemberSession(Long memberId, Long sessionId) {
        if (memberId == null || sessionId == null) {
            return false;
        }

        MemberClientSession session = getById(sessionId);
        if (session == null || !memberId.equals(session.getMemberId())) {
            return false;
        }

        return revokeSession(sessionId);
    }
}
