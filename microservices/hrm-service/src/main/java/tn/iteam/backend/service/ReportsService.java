package tn.iteam.backend.service;

import tn.iteam.backend.dto.ReportsSummaryDto;

public interface ReportsService {
    ReportsSummaryDto summary();

    byte[] exportSummaryPdf();

    byte[] exportSummaryExcel();
}
