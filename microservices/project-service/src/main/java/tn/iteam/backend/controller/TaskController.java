package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task assignment, progress and validation workflow")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "List all tasks")
    @ApiResponse(responseCode = "200", description = "Task list")
    public List<Task> all() {
        return taskService.findAll();
    }

    @GetMapping("/mine")
    @Operation(summary = "Tasks assigned to current user")
    public List<Task> mine() {
        return taskService.findMyTasks();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public Task one(@Parameter(example = "5") @PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Team leader only; assignee must be in leader team")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Business rule violation")
    })
    public Task create(@RequestBody Task task) {
        return taskService.create(task);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public Task update(@PathVariable Long id, @RequestBody Task task) {
        return taskService.update(id, task);
    }

    @PutMapping("/{id}/progress")
    @Operation(summary = "Update task progress", description = "Assignee only; respects validated progress floor")
    public Task updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int progress = body.get("progress") == null ? 0 : Integer.parseInt(body.get("progress").toString());
        return taskService.updateProgress(id, progress);
    }

    @PutMapping("/{id}/validate")
    @Operation(summary = "Validate task progress", description = "Team leader sets validated progress floor")
    public Task validate(@PathVariable Long id) {
        return taskService.validate(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
