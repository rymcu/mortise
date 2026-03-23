package com.rymcu.mortise.member.spi;

import com.rymcu.mortise.core.model.FamilyInfo;
import com.rymcu.mortise.core.spi.FamilyQueryProvider;
import com.rymcu.mortise.member.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberFamilyQueryProvider implements FamilyQueryProvider {

    private final FamilyService familyService;

    @Override
    public Optional<FamilyInfo> getFamily(Long familyId) {
        return familyService.getFamily(familyId);
    }

    @Override
    public List<FamilyInfo> listMemberFamilies(Long memberId) {
        return familyService.listMemberFamilies(memberId);
    }

    @Override
    public Optional<FamilyInfo> getCurrentFamily(Long memberId) {
        return familyService.getCurrentFamily(memberId);
    }

    @Override
    public boolean isFamilyMember(Long memberId, Long familyId) {
        return familyService.isFamilyMember(memberId, familyId);
    }

    @Override
    public boolean isFamilyAdmin(Long memberId, Long familyId) {
        return familyService.isFamilyAdmin(memberId, familyId);
    }
}
