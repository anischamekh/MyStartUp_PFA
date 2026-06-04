package tn.iteam.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.entity.TaskStatus;
import tn.iteam.backend.service.TaskService;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(tn.iteam.backend.config.OpenApiConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    @WithMockUser
    void all_returnsOk() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Demo");
        task.setStatus(TaskStatus.TODO);
        when(taskService.findAll()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Demo"));
    }

    @Test
    @WithMockUser
    void mine_returnsOk() throws Exception {
        when(taskService.findMyTasks()).thenReturn(List.of());
        mockMvc.perform(get("/api/tasks/mine")).andExpect(status().isOk());
    }
}
