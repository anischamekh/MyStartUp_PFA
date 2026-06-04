package tn.iteam.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import tn.iteam.backend.service.LeaveRequestService;

@WebMvcTest(controllers = LeaveRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(tn.iteam.backend.config.OpenApiConfig.class)
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @Test
    @WithMockUser(authorities = "HR")
    void listLeaves_returnsOk() throws Exception {
        when(leaveRequestService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/leaves")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void mine_returnsOk() throws Exception {
        when(leaveRequestService.findMine()).thenReturn(List.of());
        mockMvc.perform(get("/api/leaves/mine")).andExpect(status().isOk());
    }
}
