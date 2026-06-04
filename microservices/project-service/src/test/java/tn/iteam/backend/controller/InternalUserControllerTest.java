package tn.iteam.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.iteam.backend.repository.TaskRepository;

@WebMvcTest(controllers = InternalUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void hasActiveTasks_returnsBoolean() throws Exception {
        when(taskRepository.existsByAssignedToUserIdAndStatusIn(org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(true);

        mockMvc.perform(get("/api/internal/users/5/has-active-tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
