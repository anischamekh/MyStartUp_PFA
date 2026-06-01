package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Evaluation;

public interface EvaluationService {
    List<Evaluation> findVisible();

    Evaluation findById(Long id);

    Evaluation create(Evaluation evaluation);

    Evaluation update(Long id, Evaluation evaluation);

    void delete(Long id);
}
