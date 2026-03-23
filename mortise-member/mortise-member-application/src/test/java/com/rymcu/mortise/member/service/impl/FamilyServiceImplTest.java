package com.rymcu.mortise.member.service.impl;

import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.common.exception.BusinessException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyServiceImplTest {

    @Mock
    private FamilyMapper familyMapper;

    @Mock
    private FamilyMemberMapper familyMemberMapper;

    @Mock
    private FamilyInvitationMapper familyInvitationMapper;

    @Mock
    private MemberMapper memberMapper;

    private FamilyServiceImpl familyService;

    @BeforeEach
    void setUp() {
        familyService = new FamilyServiceImpl(
                familyMapper,
                familyMemberMapper,
                familyInvitationMapper,
                memberMapper
        );
    }

    @Test
    void shouldCreateFamilyAndAdminMembership() {
        Member owner = member(1L, null);
        when(memberMapper.selectOneByQuery(any())).thenReturn(owner);
        doAnswer(invocation -> {
            Family family = invocation.getArgument(0);
            family.setId(10L);
            return 1;
        }).when(familyMapper).insert(any(Family.class));

        Family family = familyService.createFamily(new FamilyService.CreateFamilyCommand(1L, "我的家", "客厅设备"));

        assertEquals(10L, family.getId());
        ArgumentCaptor<FamilyMember> memberCaptor = ArgumentCaptor.forClass(FamilyMember.class);
        verify(familyMemberMapper).insert(memberCaptor.capture());
        assertEquals(10L, memberCaptor.getValue().getFamilyId());
        assertEquals(1L, memberCaptor.getValue().getMemberId());
        assertEquals(FamilyMemberRole.ADMIN.getCode(), memberCaptor.getValue().getRoleCode());

        ArgumentCaptor<Member> updateCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberMapper).update(updateCaptor.capture());
        assertEquals(10L, updateCaptor.getValue().getCurrentFamilyId());
    }

    @Test
    void shouldRejectInvitingExistingFamilyMember() {
        when(familyMemberMapper.selectCountByQuery(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> familyService.inviteMember(
                new FamilyService.InviteMemberCommand(10L, 1L, 2L, "一起管理设备")));

        verify(familyInvitationMapper, never()).insert(any());
    }

    @Test
    void shouldAcceptInvitationAndCreateMembership() {
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setId(100L);
        invitation.setFamilyId(10L);
        invitation.setInviteeMemberId(2L);
        invitation.setInviterMemberId(1L);
        invitation.setStatusCode(FamilyInvitationStatus.PENDING.getCode());
        invitation.setExpiresTime(LocalDateTime.now().plusDays(1));
        invitation.setDelFlag(DelFlag.NORMAL.ordinal());

        Member member = member(2L, null);
        when(familyInvitationMapper.selectOneByQuery(any())).thenReturn(invitation);
        when(familyMemberMapper.selectCountByQuery(any())).thenReturn(0L);
        when(memberMapper.selectOneByQuery(any())).thenReturn(member);
        when(familyMemberMapper.selectListByQuery(any())).thenReturn(List.of());

        familyService.acceptInvitation(100L, 2L);

        ArgumentCaptor<FamilyMember> memberCaptor = ArgumentCaptor.forClass(FamilyMember.class);
        verify(familyMemberMapper).insert(memberCaptor.capture());
        assertEquals(10L, memberCaptor.getValue().getFamilyId());
        assertEquals(2L, memberCaptor.getValue().getMemberId());
        assertEquals(FamilyMemberRole.MEMBER.getCode(), memberCaptor.getValue().getRoleCode());
        assertEquals(FamilyInvitationStatus.ACCEPTED.getCode(), invitation.getStatusCode());
        verify(memberMapper).update(any(Member.class));
    }

    @Test
    void shouldSwitchCurrentFamilyForMember() {
        Family family = new Family();
        family.setId(10L);
        family.setFamilyName("我的家");
        family.setOwnerMemberId(1L);
        when(familyMemberMapper.selectCountByQuery(any())).thenReturn(1L);
        when(familyMapper.selectOneByQuery(any())).thenReturn(family);

        var result = familyService.switchCurrentFamily(2L, 10L);

        assertEquals(10L, result.id());
        verify(memberMapper).update(any(Member.class));
    }

    @Test
    void shouldRejectAdminLeavingFamily() {
        FamilyMember familyMember = new FamilyMember();
        familyMember.setFamilyId(10L);
        familyMember.setMemberId(1L);
        familyMember.setRoleCode(FamilyMemberRole.ADMIN.getCode());
        when(familyMemberMapper.selectOneByQuery(any())).thenReturn(familyMember);

        assertThrows(BusinessException.class, () -> familyService.leaveFamily(10L, 1L));
    }

    private Member member(Long memberId, Long currentFamilyId) {
        Member member = new Member();
        member.setId(memberId);
        member.setCurrentFamilyId(currentFamilyId);
        member.setDelFlag(DelFlag.NORMAL.ordinal());
        return member;
    }
}
