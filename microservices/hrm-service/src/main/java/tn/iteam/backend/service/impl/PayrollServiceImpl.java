package tn.iteam.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Payroll;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.service.PayrollService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserSnapshotService userSnapshotService;

    public PayrollServiceImpl(
            PayrollRepository payrollRepository,
            CurrentUserProvider currentUserProvider,
            UserSnapshotService userSnapshotService
    ) {
        this.payrollRepository = payrollRepository;
        this.currentUserProvider = currentUserProvider;
        this.userSnapshotService = userSnapshotService;
    }

    @Override
    public List<Payroll> findVisible() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        List<Payroll> rows = List.of("HR", "ADMIN").contains(me.role())
                ? payrollRepository.findAll()
                : payrollRepository.findByUserId(me.userId());
        return enrich(rows);
    }

    @Override
    public List<Payroll> findForUser(Long userId) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "ADMIN").contains(me.role()) && !me.userId().equals(userId)) {
            throw new BusinessException("Not allowed to view payroll for this user");
        }
        return enrich(payrollRepository.findByUserId(userId));
    }

    @Override
    public Payroll findById(Long id) {
        Payroll p = payrollRepository.findById(id).orElseThrow(() -> new BusinessException("Payroll not found"));
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "ADMIN").contains(me.role()) && !me.userId().equals(p.getUserId())) {
            throw new BusinessException("Not allowed to view this payroll record");
        }
        return enrich(p);
    }

    @Override
    public Payroll save(Payroll payroll) {
        requireHr();
        if (payroll.getId() != null) {
            payroll.setId(null);
        }
        Long userId = extractUserId(payroll);
        userSnapshotService.requireById(userId);
        payroll.setUserId(userId);
        recalcTotal(payroll);
        return enrich(payrollRepository.save(payroll));
    }

    @Override
    public Payroll update(Long id, Payroll payroll) {
        requireHr();
        Payroll existing = payrollRepository.findById(id).orElseThrow(() -> new BusinessException("Payroll not found"));
        existing.setBaseSalary(nz(payroll.getBaseSalary()));
        existing.setBonus(nz(payroll.getBonus()));
        existing.setDeductions(nz(payroll.getDeductions()));
        if (payroll.getUser() != null && payroll.getUser().get("id") != null) {
            Long userId = Long.valueOf(payroll.getUser().get("id").toString());
            userSnapshotService.requireById(userId);
            existing.setUserId(userId);
        }
        recalcTotal(existing);
        return enrich(payrollRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        requireHr();
        payrollRepository.deleteById(id);
    }

    private void requireHr() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"HR".equals(me.role())) {
            throw new BusinessException("Only HR can modify payroll");
        }
    }

    private Long extractUserId(Payroll payroll) {
        if (payroll.getUserId() != null) {
            return payroll.getUserId();
        }
        if (payroll.getUser() != null && payroll.getUser().get("id") != null) {
            return Long.valueOf(payroll.getUser().get("id").toString());
        }
        throw new BusinessException("user id is required");
    }

    private List<Payroll> enrich(List<Payroll> rows) {
        return rows.stream().map(this::enrich).toList();
    }

    private Payroll enrich(Payroll payroll) {
        payroll.enrichUser(userSnapshotService.findById(payroll.getUserId()).orElse(null));
        return payroll;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static void recalcTotal(Payroll p) {
        BigDecimal total = nz(p.getBaseSalary()).add(nz(p.getBonus())).subtract(nz(p.getDeductions()));
        p.setTotalSalary(total);
    }
}
