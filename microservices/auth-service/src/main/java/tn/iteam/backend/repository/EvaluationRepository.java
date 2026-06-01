package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByEmployee_Id(Long employeeId);

    List<Evaluation> findByEvaluator_Id(Long evaluatorId);

    void deleteByEmployee_Id(Long employeeId);

    void deleteByEvaluator_Id(Long evaluatorId);
}
