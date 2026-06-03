package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    void findAll_returnsTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(new Team()));
        assertEquals(1, teamService.findAll().size());
    }

    @Test
    void findById_notFound() {
        when(teamRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> teamService.findById(9L));
    }

    @Test
    void delete_removesTeam() {
        teamService.delete(3L);
        verify(teamRepository).deleteById(3L);
    }
}
