package tn.iteam.backend.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.iteam.common.dto.TeamSummaryDto;
import tn.iteam.common.dto.UserSummaryDto;

@FeignClient(name = "auth-service", url = "${app.services.auth-url}", configuration = FeignClientConfig.class)
public interface AuthServiceClient {

    @GetMapping("/api/users/{id}/summary")
    UserSummaryDto getUser(@PathVariable("id") Long id);

    @GetMapping("/api/users/summaries")
    List<UserSummaryDto> getUsers();

    @GetMapping("/api/users/team-members")
    List<UserSummaryDto> getTeamMembers();

    @GetMapping("/api/teams/{id}/summary")
    TeamSummaryDto getTeam(@PathVariable("id") Long id);

    @GetMapping("/api/teams/summaries")
    List<TeamSummaryDto> getTeams();
}
