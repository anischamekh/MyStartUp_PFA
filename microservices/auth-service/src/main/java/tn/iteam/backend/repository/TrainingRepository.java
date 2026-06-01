package tn.iteam.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {}
