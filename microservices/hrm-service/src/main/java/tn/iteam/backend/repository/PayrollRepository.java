package tn.iteam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByUserId(Long userId);

    Optional<Payroll> findFirstByUserIdOrderByIdDesc(Long userId);

    void deleteByUserId(Long userId);
}
