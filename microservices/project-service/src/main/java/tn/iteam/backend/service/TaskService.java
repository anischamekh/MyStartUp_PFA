package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Task;

public interface TaskService {
    List<Task> findAll();
    Task findById(Long id);
    List<Task> findMyTasks();
    Task create(Task task);          // team leader assigns
    Task update(Long id, Task task); // generic update
    Task updateProgress(Long id, int progress); // employee updates
    Task validate(Long id);          // team leader validates
    void delete(Long id);
}

