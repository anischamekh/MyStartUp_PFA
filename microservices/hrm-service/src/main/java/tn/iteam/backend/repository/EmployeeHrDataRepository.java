package tn.iteam.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.EmployeeHrData;

public interface EmployeeHrDataRepository extends JpaRepository<EmployeeHrData, Long> {}
