package tn.iteam.backend.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.dto.ReportsSummaryDto;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.service.ReportsService;

@Service
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    private final TaskRepository taskRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReportsServiceImpl(
            TaskRepository taskRepository,
            EmployeeProfileRepository employeeProfileRepository,
            LeaveRequestRepository leaveRequestRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.taskRepository = taskRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public ReportsSummaryDto summary() {
        requireReader();
        ReportsSummaryDto dto = new ReportsSummaryDto();
        dto.setTasksByStatus(
                taskRepository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getStatus() == null ? "UNKNOWN" : t.getStatus().name(),
                                LinkedHashMap::new,
                                Collectors.counting()
                        ))
        );
        Map<String, Long> byTeam = new LinkedHashMap<>();
        for (EmployeeProfile ep : employeeProfileRepository.findAll()) {
            String key = ep.getTeam() == null ? "No team" : ep.getTeam().getName();
            byTeam.merge(key, 1L, Long::sum);
        }
        dto.setEmployeesByTeam(byTeam);
        dto.setLeavesByStatus(
                leaveRequestRepository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                l -> l.getStatus() == null ? "UNKNOWN" : l.getStatus().name(),
                                LinkedHashMap::new,
                                Collectors.counting()
                        ))
        );
        return dto;
    }

    @Override
    public byte[] exportSummaryPdf() {
        ReportsSummaryDto s = summary();
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);
            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;
            try (PDPageContentStream cs = new PDPageContentStream(pdf, page)) {
                y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA_BOLD, 14, "HRM Reports Summary");
                y -= 8;
                y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA_BOLD, 11, "Tasks by status:");
                for (Map.Entry<String, Long> e : s.getTasksByStatus().entrySet()) {
                    y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA, 10, "  " + e.getKey() + ": " + e.getValue());
                }
                y -= 6;
                y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA_BOLD, 11, "Employees by team:");
                for (Map.Entry<String, Long> e : s.getEmployeesByTeam().entrySet()) {
                    y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA, 10, "  " + e.getKey() + ": " + e.getValue());
                }
                y -= 6;
                y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA_BOLD, 11, "Leaves by status:");
                for (Map.Entry<String, Long> e : s.getLeavesByStatus().entrySet()) {
                    y = pdfTextLine(cs, margin, y, PDType1Font.HELVETICA, 10, "  " + e.getKey() + ": " + e.getValue());
                }
            }
            pdf.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("PDF export failed: " + e.getMessage());
        }
    }

    private static float pdfTextLine(PDPageContentStream cs, float x, float y, PDType1Font font, float size, String text)
            throws IOException {
        String t = text == null ? "" : text.replace('\n', ' ').replace('\r', ' ');
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(t);
        cs.endText();
        return y - size - 4;
    }

    @Override
    public byte[] exportSummaryExcel() {
        ReportsSummaryDto s = summary();
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writeSheet(wb, "TaskStatus", s.getTasksByStatus());
            writeSheet(wb, "TeamMembers", s.getEmployeesByTeam());
            writeSheet(wb, "LeaveStatus", s.getLeavesByStatus());
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Excel export failed: " + e.getMessage());
        }
    }

    private static void writeSheet(XSSFWorkbook wb, String name, Map<String, Long> data) {
        Sheet sh = wb.createSheet(name.length() > 31 ? name.substring(0, 31) : name);
        int r = 0;
        Row header = sh.createRow(r++);
        header.createCell(0).setCellValue("Key");
        header.createCell(1).setCellValue("Count");
        for (Map.Entry<String, Long> e : data.entrySet()) {
            Row row = sh.createRow(r++);
            row.createCell(0).setCellValue(e.getKey());
            row.createCell(1).setCellValue(e.getValue() == null ? 0 : e.getValue().doubleValue());
        }
    }

    private void requireReader() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.MANAGER && rn != RoleName.ADMIN) {
            throw new BusinessException("Not allowed to view reports");
        }
    }
}
