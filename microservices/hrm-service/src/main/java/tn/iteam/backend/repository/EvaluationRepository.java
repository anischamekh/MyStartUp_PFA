package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByEmployeeId(Long employeeId);

    List<Evaluation> findByEvaluatorId(Long evaluatorId);

    void deleteByEmployeeId(Long employeeId);

    void deleteByEvaluatorId(Long evaluatorId);
}
