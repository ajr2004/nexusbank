package com.nexusbank.loan_service.scheduler;

import com.nexusbank.loan_service.client.BankingClient;
import com.nexusbank.loan_service.model.Emi;
import com.nexusbank.loan_service.model.Emi.EmiStatus;
import com.nexusbank.loan_service.model.Loan;
import com.nexusbank.loan_service.repository.EmiRepository;
import com.nexusbank.loan_service.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmiAutomationEngine {

    private final EmiRepository emiRepository;
    private final LoanRepository loanRepository;
    private final BankingClient bankingClient;

    @Scheduled(cron = "0 */2 * * * *")
    @SchedulerLock(
        name = "EmiBillingLock", 
        lockAtMostFor = "1m", 
        lockAtLeastFor = "30s"
    )
    @Transactional(rollbackFor = Exception.class)
    public void processDueEmis() {
        log.info("⚡ NexusBank Autonomous Billing Engine initiated. Scanning database for active deadlines.");

        List<Emi> dueEmis = emiRepository.findByStatusAndDueDateLessThanEqual(EmiStatus.PENDING, LocalDate.now());

        if (dueEmis.isEmpty()) {
            log.info("🏁 No outstanding due EMI records located at this time.");
            return;
        }

        log.info("Identified {} active due billing lines awaiting collection processing.", dueEmis.size());

        for (Emi emi : dueEmis) {
            Loan loan = loanRepository.findById(emi.getLoanId()).orElse(null);

            if (loan == null) {
                log.error("❌ Integrity Error: Parent Loan Contract ID {} missing for EMI ID {}. Skipping row.", 
                        emi.getLoanId(), emi.getId());
                continue;
            }

            try {
                log.info("Requesting automated direct-debit: ₹{} from Account: {} for EMI ID: {}", 
                        emi.getAmount(), loan.getAccountNumber(), emi.getId());

                bankingClient.debitAccountInternal(loan.getAccountNumber(), emi.getAmount());

                emi.setStatus(EmiStatus.PAID);
                emi.setPaymentDate(LocalDate.now());
                log.info("✅ Successfully recovered payment for EMI ID: {}. Committed status to PAID.", emi.getId());

            } catch (Exception ex) {
                log.error("⚠️ Automated payment processing failed for EMI ID: {} against Account: {}. Reason: {}", 
                        emi.getId(), loan.getAccountNumber(), ex.getMessage());
                
                emi.setStatus(EmiStatus.LATE);
            }

            emiRepository.save(emi);
        }

        log.info("⚡ NexusBank Autonomous Billing Engine processing cycle completed.");
    }
}