package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.service.EmployeeDocumentService;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Employee document management")
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    public EmployeeDocumentController(EmployeeDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(summary = "List documents", description = "Current user or HR view")
    @ApiResponse(responseCode = "200", description = "Document list")
    public List<EmployeeDocument> list() {
        return documentService.findAllForCurrentUserOrHr();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Documents for user")
    @ApiResponse(responseCode = "200", description = "User documents")
    public List<EmployeeDocument> listForUser(@Parameter(example = "3") @PathVariable Long userId) {
        return documentService.findForUser(userId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Multipart upload")
    @ApiResponse(responseCode = "200", description = "Uploaded document metadata")
    public EmployeeDocument upload(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam("file") MultipartFile file
    ) {
        return documentService.upload(userId, name, type, file);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document file")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "File stream"), @ApiResponse(responseCode = "404", description = "Not found")})
    public ResponseEntity<Resource> download(@Parameter(example = "1") @PathVariable Long id) {
        Resource resource = documentService.download(id);
        String safe = resource.getFilename() != null ? resource.getFilename() : "document";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safe.replace("\"", "") + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document")
    @ApiResponse(responseCode = "200", description = "Deleted")
    public ResponseEntity<?> delete(@Parameter(example = "1") @PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok().build();
    }
}
