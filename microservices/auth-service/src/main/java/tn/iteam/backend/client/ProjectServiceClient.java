package tn.iteam.backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "project-service", url = "${app.services.project-url}")
public interface ProjectServiceClient {

    @GetMapping("/api/internal/users/{userId}/has-active-tasks")
    boolean hasActiveTasks(@PathVariable("userId") Long userId);
}
