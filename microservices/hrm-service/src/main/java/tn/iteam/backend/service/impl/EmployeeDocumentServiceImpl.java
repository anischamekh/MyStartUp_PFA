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
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.service.EmployeeDocumentService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FileStorageProperties storageProperties;
    private final UserSnapshotService userSnapshotService;

    public EmployeeDocumentServiceImpl(
            EmployeeDocumentRepository documentRepository,
            CurrentUserProvider currentUserProvider,
            FileStorageProperties storageProperties,
            UserSnapshotService userSnapshotService
    ) {
        this.documentRepository = documentRepository;
        this.currentUserProvider = currentUserProvider;
        this.storageProperties = storageProperties;
        this.userSnapshotService = userSnapshotService;
    }

    @Override
    public List<EmployeeDocument> findAllForCurrentUserOrHr() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (List.of("HR", "ADMIN").contains(me.role())) {
            return enrich(documentRepository.findAll());
        }
        return enrich(documentRepository.findByUserIdOrderByUploadDateDesc(me.userId()));
    }

    @Override
    public List<EmployeeDocument> findForUser(Long userId) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "ADMIN").contains(me.role()) && !me.userId().equals(userId)) {
            throw new BusinessException("Not allowed to view these documents");
        }
        return enrich(documentRepository.findByUserIdOrderByUploadDateDesc(userId));
    }

    @Override
    public EmployeeDocument upload(Long userId, String name, String type, MultipartFile file) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!me.userId().equals(userId) && !List.of("HR", "ADMIN").contains(me.role())) {
            throw new BusinessException("You can only upload documents for yourself");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }
        userSnapshotService.requireById(userId);

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
            doc.setUserId(userId);
            doc.setName(name != null && !name.isBlank() ? name : (original != null ? original : "upload"));
            doc.setType(type);
            doc.setFilePath(dest.toString());
            doc.setUploadDate(LocalDateTime.now());
            return enrich(documentRepository.save(doc));
        } catch (IOException e) {
            throw new BusinessException("Could not store file: " + e.getMessage());
        }
    }

    @Override
    public Resource download(Long id) {
        EmployeeDocument doc = documentRepository.findById(id).orElseThrow(() -> new BusinessException("Document not found"));
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "ADMIN").contains(me.role()) && !me.userId().equals(doc.getUserId())) {
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
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "ADMIN").contains(me.role()) && !me.userId().equals(doc.getUserId())) {
            throw new BusinessException("Not allowed to delete this document");
        }
        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException e) {
            throw new BusinessException("Could not delete file: " + e.getMessage());
        }
        documentRepository.delete(doc);
    }

    private List<EmployeeDocument> enrich(List<EmployeeDocument> docs) {
        return docs.stream().map(this::enrich).toList();
    }

    private EmployeeDocument enrich(EmployeeDocument doc) {
        doc.enrichUser(userSnapshotService.findById(doc.getUserId()).orElse(null));
        return doc;
    }
}
