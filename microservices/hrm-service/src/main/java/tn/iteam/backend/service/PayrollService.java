package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Payroll;

public interface PayrollService {
    List<Payroll> findVisible();

    List<Payroll> findForUser(Long userId);

    Payroll findById(Long id);

    Payroll save(Payroll payroll);

    Payroll update(Long id, Payroll payroll);

    void delete(Long id);
}
