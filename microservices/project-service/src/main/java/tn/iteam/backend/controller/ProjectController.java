package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project lifecycle and team assignment")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "List all projects")
    @ApiResponse(responseCode = "200", description = "Project list")
    public List<Project> all() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by id")
    public Project one(@Parameter(example = "1") @PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create project", description = "Manager only")
    @PreAuthorize("hasAuthority('MANAGER')")
    public Project create(@RequestBody Project project) {
        return projectService.create(project);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    @PreAuthorize("hasAuthority('MANAGER')")
    public Project update(@PathVariable Long id, @RequestBody Project project) {
        return projectService.update(id, project);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
