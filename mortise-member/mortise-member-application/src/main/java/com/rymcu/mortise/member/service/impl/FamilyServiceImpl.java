package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.core.model.FamilyInfo;
import com.rymcu.mortise.member.entity.Family;
import com.rymcu.mortise.member.entity.FamilyInvitation;
import com.rymcu.mortise.member.entity.FamilyMember;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.enumerate.FamilyInvitationStatus;
import com.rymcu.mortise.member.enumerate.FamilyMemberRole;
import com.rymcu.mortise.member.mapper.FamilyInvitationMapper;
import com.rymcu.mortise.member.mapper.FamilyMapper;
import com.rymcu.mortise.member.mapper.FamilyMemberMapper;
import com.rymcu.mortise.member.mapper.MemberMapper;
import com.rymcu.mortise.member.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rymcu.mortise.member.entity.table.FamilyInvitationTableDef.FAMILY_INVITATION;
import static com.rymcu.mortise.member.entity.table.FamilyMemberTableDef.FAMILY_MEMBER;
import static com.rymcu.mortise.member.entity.table.FamilyTableDef.FAMILY;
import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final int INVITATION_EXPIRE_DAYS = 7;

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final FamilyInvitationMapper familyInvitationMapper;
    private final MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Family createFamily(CreateFamilyCommand command) {
        Member owner = requireMember(command.ownerMemberId());
        String familyName = requireFamilyName(command.familyName());
        LocalDateTime now = LocalDateTime.now();

        Family family = new Family();
        family.setFamilyName(familyName);
        family.setDescription(normalizeDescription(command.description()));
        family.setOwnerMemberId(owner.getId());
        family.setCreatedTime(now);
        family.setUpdatedTime(now);
        family.setDelFlag(DelFlag.NORMAL.ordinal());
        familyMapper.insert(family);

        FamilyMember familyMember = new FamilyMember();
        familyMember.setFamilyId(family.getId());
        familyMember.setMemberId(owner.getId());
        familyMember.setRoleCode(FamilyMemberRole.ADMIN.getCode());
        familyMember.setJoinedTime(now);
        familyMember.setCreatedTime(now);
        familyMember.setUpdatedTime(now);
        familyMember.setDelFlag(DelFlag.NORMAL.ordinal());
        familyMemberMapper.insert(familyMember);

        updateCurrentFamily(owner.getId(), family.getId());
        return family;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Family updateFamily(UpdateFamilyCommand command) {
        Family family = requireFamily(command.familyId());
        requireFamilyAdmin(command.operatorMemberId(), command.familyId());

        family.setFamilyName(requireFamilyName(command.familyName()));
        family.setDescription(normalizeDescription(command.description()));
        family.setUpdatedTime(LocalDateTime.now());
        familyMapper.update(family);
        return family;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolveFamily(Long familyId, Long operatorMemberId) {
        requireFamilyAdmin(operatorMemberId, familyId);
        Family family = requireFamily(familyId);
        LocalDateTime now = LocalDateTime.now();

        List<FamilyMember> familyMembers = familyMemberMapper.selectListByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.FAMILY_ID.eq(familyId))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        List<Long> memberIds = familyMembers.stream()
                .map(FamilyMember::getMemberId)
                .distinct()
                .toList();

        familyMembers.forEach(member -> softDeleteFamilyMember(member, now));
        familyInvitationMapper.selectListByQuery(QueryWrapper.create()
                        .where(FAMILY_INVITATION.FAMILY_ID.eq(familyId))
                        .and(FAMILY_INVITATION.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())))
                .forEach(invitation -> softDeleteInvitation(invitation, now));

        family.setUpdatedTime(now);
        family.setDelFlag(DelFlag.DELETED.ordinal());
        familyMapper.update(family);

        memberIds.forEach(this::refreshCurrentFamily);
    }

    @Override
    public List<FamilyInfo> listMemberFamilies(Long memberId) {
        requireMember(memberId);
        List<FamilyMember> memberships = listActiveMemberships(memberId);
        if (memberships.isEmpty()) {
            return List.of();
        }
        List<Long> familyIds = memberships.stream().map(FamilyMember::getFamilyId).distinct().toList();
        Map<Long, Integer> memberCounts = buildFamilyMemberCountMap(familyIds);
        return familyMapper.selectListByQuery(QueryWrapper.create()
                        .where(FAMILY.ID.in(familyIds))
                        .and(FAMILY.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                        .orderBy(FAMILY.CREATED_TIME.asc()))
                .stream()
                .map(family -> toFamilyInfo(family, memberCounts.getOrDefault(family.getId(), 0)))
                .toList();
    }

    @Override
    public Optional<FamilyInfo> getFamily(Long familyId) {
        Family family = familyMapper.selectOneByQuery(QueryWrapper.create()
                .where(FAMILY.ID.eq(familyId))
                .and(FAMILY.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (family == null) {
            return Optional.empty();
        }
        return Optional.of(toFamilyInfo(family, countFamilyMembers(familyId)));
    }

    @Override
    public Optional<FamilyInfo> getCurrentFamily(Long memberId) {
        Member member = requireMember(memberId);
        Long currentFamilyId = resolveCurrentFamilyId(member);
        if (currentFamilyId == null) {
            return Optional.empty();
        }
        return getFamily(currentFamilyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyInfo switchCurrentFamily(Long memberId, Long familyId) {
        requireFamilyMember(memberId, familyId);
        updateCurrentFamily(memberId, familyId);
        return getFamily(familyId).orElseThrow(() -> new BusinessException("家庭不存在"));
    }

    @Override
    public List<FamilyMemberView> listFamilyMembers(Long familyId, Long operatorMemberId) {
        requireFamilyMember(operatorMemberId, familyId);
        List<FamilyMember> familyMembers = familyMemberMapper.selectListByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.FAMILY_ID.eq(familyId))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                .orderBy(FAMILY_MEMBER.JOINED_TIME.asc(), FAMILY_MEMBER.ID.asc()));
        if (familyMembers.isEmpty()) {
            return List.of();
        }
        List<Long> memberIds = familyMembers.stream().map(FamilyMember::getMemberId).distinct().toList();
        Map<Long, Member> members = memberMapper.selectListByQuery(QueryWrapper.create()
                        .select(MEMBER.ID, MEMBER.NICKNAME, MEMBER.AVATAR_URL)
                        .where(MEMBER.ID.in(memberIds))
                        .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())))
                .stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (left, right) -> left, HashMap::new));
        return familyMembers.stream()
                .map(familyMember -> {
                    Member member = members.get(familyMember.getMemberId());
                    return new FamilyMemberView(
                            familyMember.getMemberId(),
                            member != null ? member.getNickname() : null,
                            member != null ? member.getAvatarUrl() : null,
                            familyMember.getRoleCode(),
                            familyMember.getJoinedTime()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyInvitation inviteMember(InviteMemberCommand command) {
        requireFamilyAdmin(command.inviterMemberId(), command.familyId());
        requireMember(command.inviteeMemberId());
        if (Objects.equals(command.inviterMemberId(), command.inviteeMemberId())) {
            throw new BusinessException("不能邀请自己加入家庭");
        }
        if (isFamilyMember(command.inviteeMemberId(), command.familyId())) {
            throw new BusinessException("该会员已加入家庭");
        }
        long pendingCount = familyInvitationMapper.selectCountByQuery(QueryWrapper.create()
                .where(FAMILY_INVITATION.FAMILY_ID.eq(command.familyId()))
                .and(FAMILY_INVITATION.INVITEE_MEMBER_ID.eq(command.inviteeMemberId()))
                .and(FAMILY_INVITATION.STATUS_CODE.eq(FamilyInvitationStatus.PENDING.getCode()))
                .and(FAMILY_INVITATION.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (pendingCount > 0) {
            throw new BusinessException("该会员已有待处理邀请");
        }

        LocalDateTime now = LocalDateTime.now();
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setFamilyId(command.familyId());
        invitation.setInviterMemberId(command.inviterMemberId());
        invitation.setInviteeMemberId(command.inviteeMemberId());
        invitation.setStatusCode(FamilyInvitationStatus.PENDING.getCode());
        invitation.setInviteMessage(normalizeInviteMessage(command.inviteMessage()));
        invitation.setExpiresTime(now.plusDays(INVITATION_EXPIRE_DAYS));
        invitation.setCreatedTime(now);
        invitation.setUpdatedTime(now);
        invitation.setDelFlag(DelFlag.NORMAL.ordinal());
        familyInvitationMapper.insert(invitation);
        return invitation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FamilyInvitationView> listPendingInvitations(Long memberId) {
        requireMember(memberId);
        List<FamilyInvitation> invitations = familyInvitationMapper.selectListByQuery(QueryWrapper.create()
                .where(FAMILY_INVITATION.INVITEE_MEMBER_ID.eq(memberId))
                .and(FAMILY_INVITATION.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                .orderBy(FAMILY_INVITATION.CREATED_TIME.desc()));
        if (invitations.isEmpty()) {
            return List.of();
        }

        List<Long> familyIds = invitations.stream().map(FamilyInvitation::getFamilyId).distinct().toList();
        Map<Long, Family> familyMap = familyMapper.selectListByQuery(QueryWrapper.create()
                        .where(FAMILY.ID.in(familyIds))
                        .and(FAMILY.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())))
                .stream()
                .collect(Collectors.toMap(Family::getId, Function.identity()));

        List<Long> inviterIds = invitations.stream().map(FamilyInvitation::getInviterMemberId).distinct().toList();
        Map<Long, Member> inviterMap = memberMapper.selectListByQuery(QueryWrapper.create()
                        .select(MEMBER.ID, MEMBER.NICKNAME)
                        .where(MEMBER.ID.in(inviterIds))
                        .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())))
                .stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        return invitations.stream()
                .map(invitation -> expireIfNecessary(invitation, now))
                .filter(invitation -> FamilyInvitationStatus.PENDING.getCode().equals(invitation.getStatusCode()))
                .map(invitation -> {
                    Family family = familyMap.get(invitation.getFamilyId());
                    Member inviter = inviterMap.get(invitation.getInviterMemberId());
                    return new FamilyInvitationView(
                            invitation.getId(),
                            invitation.getFamilyId(),
                            family != null ? family.getFamilyName() : null,
                            invitation.getInviterMemberId(),
                            inviter != null ? inviter.getNickname() : null,
                            invitation.getInviteeMemberId(),
                            invitation.getInviteMessage(),
                            invitation.getStatusCode(),
                            invitation.getExpiresTime(),
                            invitation.getCreatedTime()
                    );
                })
                .toList();
    }

    @Override
    public Optional<FamilyInvitationView> getInvitationView(Long invitationId) {
        FamilyInvitation invitation = familyInvitationMapper.selectOneByQuery(QueryWrapper.create()
                .where(FAMILY_INVITATION.ID.eq(invitationId))
                .and(FAMILY_INVITATION.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (invitation == null) {
            return Optional.empty();
        }
        Family family = familyMapper.selectOneByQuery(QueryWrapper.create()
                .select(FAMILY.ID, FAMILY.FAMILY_NAME)
                .where(FAMILY.ID.eq(invitation.getFamilyId()))
                .and(FAMILY.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        Member inviter = memberMapper.selectOneByQuery(QueryWrapper.create()
                .select(MEMBER.ID, MEMBER.NICKNAME)
                .where(MEMBER.ID.eq(invitation.getInviterMemberId()))
                .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        return Optional.of(new FamilyInvitationView(
                invitation.getId(),
                invitation.getFamilyId(),
                family != null ? family.getFamilyName() : null,
                invitation.getInviterMemberId(),
                inviter != null ? inviter.getNickname() : null,
                invitation.getInviteeMemberId(),
                invitation.getInviteMessage(),
                invitation.getStatusCode(),
                invitation.getExpiresTime(),
                invitation.getCreatedTime()
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyInvitation acceptInvitation(Long invitationId, Long memberId) {
        FamilyInvitation invitation = requireInvitation(invitationId);
        if (!Objects.equals(invitation.getInviteeMemberId(), memberId)) {
            throw new BusinessException("无权处理该邀请");
        }
        invitation = expireIfNecessary(invitation, LocalDateTime.now());
        if (!FamilyInvitationStatus.PENDING.getCode().equals(invitation.getStatusCode())) {
            throw new BusinessException("邀请已失效");
        }
        if (isFamilyMember(memberId, invitation.getFamilyId())) {
            throw new BusinessException("已加入该家庭");
        }

        LocalDateTime now = LocalDateTime.now();
        FamilyMember familyMember = new FamilyMember();
        familyMember.setFamilyId(invitation.getFamilyId());
        familyMember.setMemberId(memberId);
        familyMember.setRoleCode(FamilyMemberRole.MEMBER.getCode());
        familyMember.setJoinedTime(now);
        familyMember.setCreatedTime(now);
        familyMember.setUpdatedTime(now);
        familyMember.setDelFlag(DelFlag.NORMAL.ordinal());
        familyMemberMapper.insert(familyMember);

        invitation.setStatusCode(FamilyInvitationStatus.ACCEPTED.getCode());
        invitation.setRepliedTime(now);
        invitation.setUpdatedTime(now);
        familyInvitationMapper.update(invitation);

        Member member = requireMember(memberId);
        if (resolveCurrentFamilyId(member) == null) {
            updateCurrentFamily(memberId, invitation.getFamilyId());
        }
        return invitation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyInvitation rejectInvitation(Long invitationId, Long memberId) {
        FamilyInvitation invitation = requireInvitation(invitationId);
        if (!Objects.equals(invitation.getInviteeMemberId(), memberId)) {
            throw new BusinessException("无权处理该邀请");
        }
        invitation = expireIfNecessary(invitation, LocalDateTime.now());
        if (!FamilyInvitationStatus.PENDING.getCode().equals(invitation.getStatusCode())) {
            throw new BusinessException("邀请已失效");
        }
        invitation.setStatusCode(FamilyInvitationStatus.REJECTED.getCode());
        invitation.setRepliedTime(LocalDateTime.now());
        invitation.setUpdatedTime(LocalDateTime.now());
        familyInvitationMapper.update(invitation);
        return invitation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyInvitation cancelInvitation(Long invitationId, Long operatorMemberId) {
        FamilyInvitation invitation = requireInvitation(invitationId);
        requireFamilyAdmin(operatorMemberId, invitation.getFamilyId());
        invitation = expireIfNecessary(invitation, LocalDateTime.now());
        if (!FamilyInvitationStatus.PENDING.getCode().equals(invitation.getStatusCode())) {
            throw new BusinessException("邀请已失效");
        }
        invitation.setStatusCode(FamilyInvitationStatus.CANCELLED.getCode());
        invitation.setUpdatedTime(LocalDateTime.now());
        familyInvitationMapper.update(invitation);
        return invitation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(RemoveFamilyMemberCommand command) {
        requireFamilyAdmin(command.operatorMemberId(), command.familyId());
        if (Objects.equals(command.operatorMemberId(), command.targetMemberId())) {
            throw new BusinessException("不能通过移除成员接口移除自己");
        }
        FamilyMember member = requireActiveFamilyMember(command.targetMemberId(), command.familyId());
        if (FamilyMemberRole.ADMIN.getCode().equals(member.getRoleCode())) {
            throw new BusinessException("不能移除家庭管理员");
        }
        softDeleteFamilyMember(member, LocalDateTime.now());
        refreshCurrentFamily(command.targetMemberId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveFamily(Long familyId, Long memberId) {
        FamilyMember member = requireActiveFamilyMember(memberId, familyId);
        if (FamilyMemberRole.ADMIN.getCode().equals(member.getRoleCode())) {
            throw new BusinessException("家庭管理员不能直接退出，请先解散家庭");
        }
        softDeleteFamilyMember(member, LocalDateTime.now());
        refreshCurrentFamily(memberId);
    }

    @Override
    public boolean isFamilyMember(Long memberId, Long familyId) {
        return familyMemberMapper.selectCountByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.FAMILY_ID.eq(familyId))
                .and(FAMILY_MEMBER.MEMBER_ID.eq(memberId))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))) > 0;
    }

    @Override
    public boolean isFamilyAdmin(Long memberId, Long familyId) {
        return familyMemberMapper.selectCountByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.FAMILY_ID.eq(familyId))
                .and(FAMILY_MEMBER.MEMBER_ID.eq(memberId))
                .and(FAMILY_MEMBER.ROLE_CODE.eq(FamilyMemberRole.ADMIN.getCode()))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))) > 0;
    }

    private Family requireFamily(Long familyId) {
        Family family = familyMapper.selectOneByQuery(QueryWrapper.create()
                .where(FAMILY.ID.eq(familyId))
                .and(FAMILY.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }
        return family;
    }

    private Member requireMember(Long memberId) {
        Member member = memberMapper.selectOneByQuery(QueryWrapper.create()
                .where(MEMBER.ID.eq(memberId))
                .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        return member;
    }

    private String requireFamilyName(String familyName) {
        if (!StringUtils.hasText(familyName)) {
            throw new BusinessException("家庭名称不能为空");
        }
        String normalized = familyName.trim();
        if (normalized.length() > 100) {
            throw new BusinessException("家庭名称长度不能超过100个字符");
        }
        return normalized;
    }

    private String normalizeDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }
        String normalized = description.trim();
        if (normalized.length() > 500) {
            throw new BusinessException("家庭描述长度不能超过500个字符");
        }
        return normalized;
    }

    private String normalizeInviteMessage(String inviteMessage) {
        if (!StringUtils.hasText(inviteMessage)) {
            return null;
        }
        String normalized = inviteMessage.trim();
        if (normalized.length() > 255) {
            throw new BusinessException("邀请附言长度不能超过255个字符");
        }
        return normalized;
    }

    private FamilyMember requireActiveFamilyMember(Long memberId, Long familyId) {
        FamilyMember familyMember = familyMemberMapper.selectOneByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.FAMILY_ID.eq(familyId))
                .and(FAMILY_MEMBER.MEMBER_ID.eq(memberId))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (familyMember == null) {
            throw new BusinessException("家庭成员不存在");
        }
        return familyMember;
    }

    private void requireFamilyMember(Long memberId, Long familyId) {
        if (!isFamilyMember(memberId, familyId)) {
            throw new BusinessException("当前会员不属于该家庭");
        }
    }

    private void requireFamilyAdmin(Long memberId, Long familyId) {
        if (!isFamilyAdmin(memberId, familyId)) {
            throw new BusinessException("仅家庭管理员可执行该操作");
        }
    }

    private FamilyInvitation requireInvitation(Long invitationId) {
        FamilyInvitation invitation = familyInvitationMapper.selectOneByQuery(QueryWrapper.create()
                .where(FAMILY_INVITATION.ID.eq(invitationId))
                .and(FAMILY_INVITATION.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
        if (invitation == null) {
            throw new BusinessException("邀请不存在");
        }
        return invitation;
    }

    private int countFamilyMembers(Long familyId) {
        return (int) familyMemberMapper.selectCountByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.FAMILY_ID.eq(familyId))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
    }

    private Map<Long, Integer> buildFamilyMemberCountMap(List<Long> familyIds) {
        if (familyIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> counts = new HashMap<>();
        familyMemberMapper.selectListByQuery(QueryWrapper.create()
                        .select(FAMILY_MEMBER.FAMILY_ID)
                        .where(FAMILY_MEMBER.FAMILY_ID.in(familyIds))
                        .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())))
                .forEach(member -> counts.merge(member.getFamilyId(), 1, Integer::sum));
        return counts;
    }

    private FamilyInfo toFamilyInfo(Family family, int memberCount) {
        return new FamilyInfo(
                family.getId(),
                family.getFamilyName(),
                family.getDescription(),
                family.getOwnerMemberId(),
                memberCount
        );
    }

    private List<FamilyMember> listActiveMemberships(Long memberId) {
        return familyMemberMapper.selectListByQuery(QueryWrapper.create()
                .where(FAMILY_MEMBER.MEMBER_ID.eq(memberId))
                .and(FAMILY_MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                .orderBy(FAMILY_MEMBER.JOINED_TIME.asc(), FAMILY_MEMBER.ID.asc()));
    }

    private Long resolveCurrentFamilyId(Member member) {
        if (member.getCurrentFamilyId() != null && isFamilyMember(member.getId(), member.getCurrentFamilyId())) {
            return member.getCurrentFamilyId();
        }
        List<FamilyMember> memberships = listActiveMemberships(member.getId());
        if (memberships.isEmpty()) {
            if (member.getCurrentFamilyId() != null) {
                updateCurrentFamily(member.getId(), null);
            }
            return null;
        }
        Long fallbackFamilyId = memberships.getFirst().getFamilyId();
        updateCurrentFamily(member.getId(), fallbackFamilyId);
        return fallbackFamilyId;
    }

    private void updateCurrentFamily(Long memberId, Long familyId) {
        Member update = new Member();
        update.setId(memberId);
        update.setCurrentFamilyId(familyId);
        update.setUpdatedTime(LocalDateTime.now());
        memberMapper.update(update);
    }

    private void refreshCurrentFamily(Long memberId) {
        Member member = requireMember(memberId);
        resolveCurrentFamilyId(member);
    }

    private void softDeleteFamilyMember(FamilyMember familyMember, LocalDateTime now) {
        familyMember.setDelFlag(DelFlag.DELETED.ordinal());
        familyMember.setUpdatedTime(now);
        familyMemberMapper.update(familyMember);
    }

    private void softDeleteInvitation(FamilyInvitation invitation, LocalDateTime now) {
        invitation.setDelFlag(DelFlag.DELETED.ordinal());
        invitation.setUpdatedTime(now);
        familyInvitationMapper.update(invitation);
    }

    private FamilyInvitation expireIfNecessary(FamilyInvitation invitation, LocalDateTime now) {
        if (FamilyInvitationStatus.PENDING.getCode().equals(invitation.getStatusCode())
                && invitation.getExpiresTime() != null
                && invitation.getExpiresTime().isBefore(now)) {
            invitation.setStatusCode(FamilyInvitationStatus.EXPIRED.getCode());
            invitation.setUpdatedTime(now);
            familyInvitationMapper.update(invitation);
        }
        return invitation;
    }
}
