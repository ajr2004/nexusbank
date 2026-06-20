package com.nexusbank.loan_service.service;

import com.nexusbank.loan_service.dto.EmiDto;
import com.nexusbank.loan_service.model.Emi;
import com.nexusbank.loan_service.model.Loan;
import com.nexusbank.loan_service.model.Emi.EmiStatus;
import com.nexusbank.loan_service.repository.EmiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiService {

    private final EmiRepository emiRepository;

    private static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    // Overloaded method to maintain backward compatibility if called elsewhere without a string tag
    @Transactional(rollbackFor = Exception.class)
    public void generateSchedule(Loan loan) {
        generateSchedule(loan, "DEFAULT_GENERATION");
    }

    // 🔒 Enforce atomic transactional rollback guarantees
    @Transactional(rollbackFor = Exception.class)
    public void generateSchedule(Loan loan, String trackingSource) {

        // 🚫 Prevent duplicate EMI generation
        if (emiRepository.countByLoanId(loan.getId()) > 0) {
            log.warn("EMI schedule already exists for Loan ID: {}. Generation skipped.", loan.getId());
            return;
        }

        BigDecimal monthlyEmi = calculateEmi(
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths()
        ).setScale(2, RM);

        int totalMonths = loan.getTenureMonths();
        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        // 🏛️ True Banking Standard: Track the reducing principal balance
        BigDecimal remainingPrincipal = loan.getAmount(); 
        List<Emi> emis = new ArrayList<>();

        for (int i = 0; i < totalMonths; i++) {
            
            // 1. Calculate Monthly Interest Component = (Remaining Principal * (Annual Rate / 12)) / 100
            BigDecimal monthlyInterest = remainingPrincipal
                    .multiply(BigDecimal.valueOf(loan.getInterestRate()), MC)
                    .divide(BigDecimal.valueOf(1200), MC);

            // 2. Calculate Principal Component = Total EMI - Monthly Interest
            BigDecimal principalComponent = monthlyEmi.subtract(monthlyInterest, MC);

            // 3. Reduce the outstanding principal debt
            remainingPrincipal = remainingPrincipal.subtract(principalComponent, MC);

            // 4. Handle rounding tolerance rules for the final installment drop
            if (remainingPrincipal.signum() < 0 || (i == totalMonths - 1)) {
                remainingPrincipal = BigDecimal.ZERO;
            }

            Emi emi = Emi.builder()
                    .loanId(loan.getId())
                    .amount(monthlyEmi)
                    .dueDate(firstDueDate.plusMonths(i))
                    .status(EmiStatus.PENDING)
                    .remainingBalance(remainingPrincipal.setScale(2, RM))
                    .build();

            emis.add(emi);
        }

        emiRepository.saveAll(emis);
        log.info("Successfully generated {} amortized EMI schedule records for Loan ID: {} via source: {}", 
                emis.size(), loan.getId(), trackingSource);
    }

    // 💡 EMI FORMULA: Standard Amortization Math (Symmetric and solid)
    private BigDecimal calculateEmi(BigDecimal principal, double annualRate, int months) {
        if (months <= 0) return BigDecimal.ZERO;

        BigDecimal r = BigDecimal.valueOf(annualRate).divide(BigDecimal.valueOf(1200), MC);

        if (r.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), MC);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(r, MC);
        BigDecimal pow = onePlusR.pow(months, MC);

        BigDecimal numerator = principal.multiply(r, MC).multiply(pow, MC);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE, MC);

        return numerator.divide(denominator, MC);
    }

    public List<EmiDto> getEmisByLoanId(Long loanId) {
        return emiRepository.findByLoanIdOrderByDueDateAsc(loanId)
                .stream()
                .map(emi -> new EmiDto(
                        emi.getId(),
                        emi.getAmount(),
                        emi.getDueDate(),
                        emi.getStatus().name(),
                        emi.getRemainingBalance()
                ))
                .toList();
    }
}