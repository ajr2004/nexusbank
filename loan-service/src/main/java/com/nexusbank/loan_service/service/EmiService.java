package com.nexusbank.loan_service.service;

import com.nexusbank.loan_service.dto.EmiDto;
import com.nexusbank.loan_service.model.Emi;
import com.nexusbank.loan_service.model.Loan;
import com.nexusbank.loan_service.model.Emi.EmiStatus;
import com.nexusbank.loan_service.repository.EmiRepository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmiService {

    private final EmiRepository emiRepository;

    private static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public void generateSchedule(Loan loan) {

        // 🚫 Prevent duplicate EMI generation
        if (emiRepository.countByLoanId(loan.getId()) > 0) {
            return;
        }

        BigDecimal monthlyEmi = calculateEmi(
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths()
        ).setScale(2, RM);

        int totalMonths = loan.getTenureMonths();
        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        BigDecimal totalRepayable = monthlyEmi.multiply(BigDecimal.valueOf(totalMonths), MC);
        BigDecimal remainingBalance = totalRepayable;

        List<Emi> emis = new ArrayList<>();

        for (int i = 0; i < totalMonths; i++) {

            remainingBalance = remainingBalance.subtract(monthlyEmi, MC);
            if (remainingBalance.signum() < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            Emi emi = Emi.builder()
                    .loanId(loan.getId())
                    .amount(monthlyEmi)
                    .dueDate(firstDueDate.plusMonths(i))
                    .status(EmiStatus.PENDING) // ✅ FIXED (ENUM)
                    .remainingBalance(remainingBalance.setScale(2, RM))
                    .build();

            emis.add(emi);
        }

        emiRepository.saveAll(emis);
    }

    // 💡 EMI FORMULA
    private BigDecimal calculateEmi(BigDecimal principal, double annualRate, int months) {

        if (months <= 0) return BigDecimal.ZERO;

        BigDecimal r = BigDecimal.valueOf(annualRate)
                .divide(BigDecimal.valueOf(1200), MC); // monthly rate

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
                        emi.getStatus().name(),   // ✅ FIX
                        emi.getRemainingBalance()
                ))
                .toList();
    }
}