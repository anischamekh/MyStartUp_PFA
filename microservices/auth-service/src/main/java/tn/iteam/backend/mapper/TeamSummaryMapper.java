package tn.iteam.backend.mapper;

import tn.iteam.backend.entity.Team;
import tn.iteam.common.dto.TeamSummaryDto;

public final class TeamSummaryMapper {

    private TeamSummaryMapper() {}

    public static TeamSummaryDto toDto(Team team) {
        return new TeamSummaryDto(
                team.getId(),
                team.getName(),
                team.getTeamLeader() == null ? null : team.getTeamLeader().getId()
        );
    }
}
