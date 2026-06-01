package tn.iteam.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Payroll;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.PayrollService;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public PayrollServiceImpl(
            PayrollRepository payrollRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<Payroll> findVisible() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.ADMIN) {
            return payrollRepository.findAll();
        }
        return payrollRepository.findByUser_Id(me.getId());
    }

    @Override
    public List<Payroll> findForUser(Long userId) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN && !me.getId().equals(userId)) {
            throw new BusinessException("Not allowed to view payroll for this user");
        }
        return payrollRepository.findByUser_Id(userId);
    }

    @Override
    public Payroll findById(Long id) {
        Payroll p = payrollRepository.findById(id).orElseThrow(() -> new BusinessException("Payroll not found"));
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN && !p.getUser().getId().equals(me.getId())) {
            throw new BusinessException("Not allowed to view this payroll record");
        }
        return p;
    }

    @Override
    public Payroll save(Payroll payroll) {
        requireHr();
        if (payroll.getId() != null) {
            payroll.setId(null);
        }
        User u = userRepository.findById(payroll.getUser().getId())
                .orElseThrow(() -> new BusinessException("User not found"));
        payroll.setUser(u);
        recalcTotal(payroll);
        return payrollRepository.save(payroll);
    }

    @Override
    public Payroll update(Long id, Payroll payroll) {
        requireHr();
        Payroll existing = payrollRepository.findById(id).orElseThrow(() -> new BusinessException("Payroll not found"));
        existing.setBaseSalary(nz(payroll.getBaseSalary()));
        existing.setBonus(nz(payroll.getBonus()));
        existing.setDeductions(nz(payroll.getDeductions()));
        if (payroll.getUser() != null && payroll.getUser().getId() != null) {
            User u = userRepository.findById(payroll.getUser().getId())
                    .orElseThrow(() -> new BusinessException("User not found"));
            existing.setUser(u);
        }
        recalcTotal(existing);
        return payrollRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        requireHr();
        payrollRepository.deleteById(id);
    }

    private void requireHr() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR) {
            throw new BusinessException("Only HR can modify payroll");
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static void recalcTotal(Payroll p) {
        BigDecimal total = nz(p.getBaseSalary()).add(nz(p.getBonus())).subtract(nz(p.getDeductions()));
        p.setTotalSalary(total);
    }
}
