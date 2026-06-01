package tn.iteam.backend.service;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import tn.iteam.backend.entity.EmployeeDocument;

public interface EmployeeDocumentService {
    List<EmployeeDocument> findAllForCurrentUserOrHr();

    List<EmployeeDocument> findForUser(Long userId);

    EmployeeDocument upload(Long userId, String name, String type, MultipartFile file);

    Resource download(Long id);

    void delete(Long id);
}
