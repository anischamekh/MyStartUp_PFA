package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import tn.iteam.backend.entity.Skill;
import tn.iteam.backend.service.SkillService;
import tn.iteam.common.openapi.OpenApiExamples;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Skills catalog management (HR)")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @Operation(summary = "List skills", description = "Returns all skills in the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @io.swagger.v3.oas.annotations.media.Content(examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = OpenApiExamples.ERROR_RESPONSE)))
    })
    public List<Skill> all() {
        return skillService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Skill one(@Parameter(description = "Skill id", example = "1") @PathVariable Long id) {
        return skillService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create skill", description = "HR only")
    @ApiResponse(responseCode = "200", description = "Created skill")
    public Skill create(@RequestBody Skill skill) {
        return skillService.save(skill);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update skill", description = "HR only")
    @ApiResponse(responseCode = "200", description = "Updated skill")
    public Skill update(
            @Parameter(description = "Skill id", example = "1") @PathVariable Long id,
            @RequestBody Skill skill) {
        return skillService.update(id, skill);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete skill", description = "HR only")
    @ApiResponse(responseCode = "200", description = "Deleted")
    public ResponseEntity<?> delete(@Parameter(description = "Skill id", example = "1") @PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.ok().build();
    }
}
