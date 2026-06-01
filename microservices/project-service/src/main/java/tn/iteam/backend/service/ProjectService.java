package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Project;

public interface ProjectService {
    List<Project> findAll();
    Project findById(Long id);
    Project create(Project project);
    Project update(Long id, Project project);
    void delete(Long id);
}

