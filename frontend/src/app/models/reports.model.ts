export interface ReportsSummary {
  tasksByStatus: Record<string, number>;
  employeesByTeam: Record<string, number>;
  leavesByStatus: Record<string, number>;
}
