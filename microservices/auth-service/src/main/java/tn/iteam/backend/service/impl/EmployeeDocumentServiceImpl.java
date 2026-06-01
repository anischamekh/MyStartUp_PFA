package tn.iteam.backend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.iteam.backend.config.FileStorageProperties;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.EmployeeDocumentService;

@Service
@Transactional
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FileStorageProperties storageProperties;

    public EmployeeDocumentServiceImpl(
            EmployeeDocumentRepository documentRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            FileStorageProperties storageProperties
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.storageProperties = storageProperties;
    }

    @Override
    public List<EmployeeDocument> findAllForCurrentUserOrHr() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.ADMIN) {
            return documentRepository.findAll();
        }
        return documentRepository.findByUser_IdOrderByUploadDateDesc(me.getId());
    }

    @Override
    public List<EmployeeDocument> findForUser(Long userId) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN && !me.getId().equals(userId)) {
            throw new BusinessException("Not allowed to view these documents");
        }
        return documentRepository.findByUser_IdOrderByUploadDateDesc(userId);
    }

    @Override
    public EmployeeDocument upload(Long userId, String name, String type, MultipartFile file) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (!me.getId().equals(userId) && rn != RoleName.HR && rn != RoleName.ADMIN) {
            throw new BusinessException("You can only upload documents for yourself");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }
        User owner = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        String safeName = UUID.randomUUID() + ext;

        Path root = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            Path dest = root.resolve(safeName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            EmployeeDocument doc = new EmployeeDocument();
            doc.setUser(owner);
            doc.setName(name != null && !name.isBlank() ? name : (original != null ? original : "upload"));
            doc.setType(type);
            doc.setFilePath(dest.toString());
            doc.setUploadDate(LocalDateTime.now());
            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new BusinessException("Could not store file: " + e.getMessage());
        }
    }

    @Override
    public Resource download(Long id) {
        EmployeeDocument doc = documentRepository.findById(id).orElseThrow(() -> new BusinessException("Document not found"));
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN && !doc.getUser().getId().equals(me.getId())) {
            throw new BusinessException("Not allowed to download this document");
        }
        Path p = Paths.get(doc.getFilePath());
        if (!Files.exists(p)) {
            throw new BusinessException("File missing on server");
        }
        return new FileSystemResource(p);
    }

    @Override
    public void delete(Long id) {
        EmployeeDocument doc = documentRepository.findById(id).orElseThrow(() -> new BusinessException("Document not found"));
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN && !doc.getUser().getId().equals(me.getId())) {
            throw new BusinessException("Not allowed to delete this document");
        }
        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException e) {
            throw new BusinessException("Could not delete file: " + e.getMessage());
        }
        documentRepository.delete(doc);
    }
}
