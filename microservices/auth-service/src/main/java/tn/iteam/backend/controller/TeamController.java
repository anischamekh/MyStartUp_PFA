package tn.iteam.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.mapper.TeamSummaryMapper;
import tn.iteam.backend.service.TeamService;
import tn.iteam.common.dto.TeamSummaryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Team management")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "List teams")
    @ApiResponse(responseCode = "200", description = "Team list")
    public List<Team> all() {
        return teamService.findAll();
    }

    @GetMapping("/{id}")
    public Team one(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @GetMapping("/{id}/summary")
    public TeamSummaryDto summary(@PathVariable Long id) {
        return TeamSummaryMapper.toDto(teamService.findById(id));
    }

    @GetMapping("/summaries")
    public java.util.List<TeamSummaryDto> summaries() {
        return teamService.findAll().stream().map(TeamSummaryMapper::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public Team create(@RequestBody Team team) {
        return teamService.create(team);
    }

    @PutMapping("/{id}")
    public Team update(@PathVariable Long id, @RequestBody Team team) {
        return teamService.update(id, team);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.ok().build();
    }
}

