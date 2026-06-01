package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.dto.ReportsSummaryDto;
import tn.iteam.backend.service.ReportsService;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "HR analytics and exports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "HRM summary dashboard data")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Summary JSON"), @ApiResponse(responseCode = "403", description = "Forbidden")})
    public ReportsSummaryDto summary() {
        return reportsService.summary();
    }

    @GetMapping("/export/summary.pdf")
    @Operation(summary = "Export summary as PDF")
    @ApiResponse(responseCode = "200", description = "PDF bytes")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] data = reportsService.exportSummaryPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hrm-summary.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/export/summary.xlsx")
    @Operation(summary = "Export summary as Excel")
    @ApiResponse(responseCode = "200", description = "XLSX bytes")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = reportsService.exportSummaryExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hrm-summary.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
