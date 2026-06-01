package tn.iteam.backend.mapper;

import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.User;
import tn.iteam.common.dto.UserSummaryDto;

public final class UserSummaryMapper {

    private UserSummaryMapper() {}

    public static UserSummaryDto toDto(User user, EmployeeProfile profile) {
        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().getName().name(),
                profile == null || profile.getTeam() == null ? null : profile.getTeam().getId(),
                profile == null || profile.getTeam() == null ? null : profile.getTeam().getName()
        );
    }
}
