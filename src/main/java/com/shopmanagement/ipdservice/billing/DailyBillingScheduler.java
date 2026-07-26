package com.shopmanagement.ipdservice.billing;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyBillingScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyBillingScheduler.class);

    private final DailyBillingService dailyBillingService;

    @Value("${ipd.billing.timezone:Asia/Kolkata}")
    private String timezone;

    @Value("${ipd.billing.daily-enabled:true}")
    private boolean enabled;

    public DailyBillingScheduler(DailyBillingService dailyBillingService) {
        this.dailyBillingService = dailyBillingService;
    }

    /** Runs shortly after midnight local hospital time — posts charges then syncs to order-service. */
    @Scheduled(cron = "${ipd.billing.daily-cron:0 5 0 * * *}", zone = "${ipd.billing.timezone:Asia/Kolkata}")
    public void midnightRun() {
        if (!enabled) {
            return;
        }
        LocalDate chargeDate = LocalDate.now(ZoneId.of(timezone)).minusDays(1);
        try {
            Map<String, Object> result = dailyBillingService.runForAllTenants(chargeDate);
            log.info("IPD midnight billing + sync completed: {}", result);
        } catch (Exception ex) {
            log.error("IPD midnight billing failed for {}", chargeDate, ex);
        }
    }
}
