package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.EmployeeDocument;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findByUser_IdOrderByUploadDateDesc(Long userId);
}
