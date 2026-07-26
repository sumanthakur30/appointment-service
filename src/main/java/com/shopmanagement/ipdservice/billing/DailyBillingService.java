package com.shopmanagement.ipdservice.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.diet.DietPlan;
import com.shopmanagement.ipdservice.diet.DietPlanRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class DailyBillingService {

    private static final Logger log = LoggerFactory.getLogger(DailyBillingService.class);
    private static final List<String> ACTIVE = List.of("ADMITTED", "TRANSFERRED");
    private static final BigDecimal DEFAULT_BED = new BigDecimal("1500.00");
    private static final BigDecimal DEFAULT_NURSING = new BigDecimal("300.00");
    private static final BigDecimal DEFAULT_FOOD = new BigDecimal("250.00");

    private final IpdAdmissionRepository admissionRepository;
    private final AccommodationClient accommodationClient;
    private final IpdChargeLineRepository chargeRepository;
    private final DietPlanRepository dietPlanRepository;
    private final ChargeSyncService chargeSyncService;

    public DailyBillingService(
            IpdAdmissionRepository admissionRepository,
            AccommodationClient accommodationClient,
            IpdChargeLineRepository chargeRepository,
            DietPlanRepository dietPlanRepository,
            ChargeSyncService chargeSyncService) {
        this.admissionRepository = admissionRepository;
        this.accommodationClient = accommodationClient;
        this.chargeRepository = chargeRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.chargeSyncService = chargeSyncService;
    }

    /** Batch for all tenants (scheduler). */
    @Transactional
    public Map<String, Object> runForAllTenants(LocalDate chargeDate) {
        List<IpdAdmission> active = admissionRepository.findByStatusIn(ACTIVE);
        int created = 0;
        int skipped = 0;
        for (IpdAdmission a : active) {
            int n = postDailyCharges(a, chargeDate);
            if (n > 0) {
                created += n;
            } else {
                skipped++;
            }
            try {
                chargeSyncService.syncAdmissionDay(a, chargeDate);
            } catch (Exception ex) {
                log.warn("Sync after daily billing failed admission={}: {}", a.getId(), ex.getMessage());
            }
        }
        log.info("IPD daily billing {} — admissions={}, linesCreated={}, alreadyPosted={}",
                chargeDate, active.size(), created, skipped);
        Map<String, Object> result = new HashMap<>();
        result.put("chargeDate", chargeDate.toString());
        result.put("admissions", active.size());
        result.put("linesCreated", created);
        result.put("alreadyPosted", skipped);
        return result;
    }

    /** Tenant-scoped manual run + best-effort sync to order-service. */
    @Transactional
    public Map<String, Object> runForCurrentTenant(LocalDate chargeDate) {
        List<IpdAdmission> active = admissionRepository.findByTenantIdAndShopIdAndStatusInOrderByAdmittedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), ACTIVE);
        int created = 0;
        int synced = 0;
        for (IpdAdmission a : active) {
            created += postDailyCharges(a, chargeDate);
            try {
                Map<String, Object> sync = chargeSyncService.syncAdmissionDay(a, chargeDate);
                if (Boolean.TRUE.equals(sync.get("synced"))) {
                    synced++;
                }
            } catch (Exception ex) {
                log.warn("Sync after daily billing failed admission={}: {}", a.getId(), ex.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("chargeDate", chargeDate.toString());
        result.put("admissions", active.size());
        result.put("linesCreated", created);
        result.put("syncedAdmissions", synced);
        return result;
    }

    public List<IpdChargeLine> listForAdmission(Long admissionId) {
        return chargeRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByChargeDateDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    public List<IpdChargeLine> listForDate(LocalDate chargeDate) {
        return chargeRepository.findByTenantIdAndShopIdAndChargeDateOrderByAdmissionNoAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), chargeDate);
    }

    private int postDailyCharges(IpdAdmission a, LocalDate chargeDate) {
        List<IpdChargeLine> lines = new ArrayList<>();
        BigDecimal bedRate = resolveBedRate(a);
        lines.add(buildLine(a, chargeDate, "BED_RENT", "Daily room/bed rent", bedRate));
        lines.add(buildLine(a, chargeDate, "NURSING", "Daily nursing charges", DEFAULT_NURSING));

        DietPlan diet = dietPlanRepository
                .findFirstByTenantIdAndShopIdAndAdmissionIdAndActiveTrueOrderByEffectiveFromDesc(
                        a.getTenantId(), a.getShopId(), a.getId())
                .orElse(null);
        if (diet == null || !"NPO".equalsIgnoreCase(diet.getDietType())) {
            lines.add(buildLine(a, chargeDate, "FOOD", "Daily diet / food", DEFAULT_FOOD));
        }

        int created = 0;
        for (IpdChargeLine line : lines) {
            boolean exists = chargeRepository
                    .findByTenantIdAndShopIdAndAdmissionIdAndChargeDateAndChargeType(
                            a.getTenantId(), a.getShopId(), a.getId(), chargeDate, line.getChargeType())
                    .isPresent();
            if (!exists) {
                chargeRepository.save(line);
                created++;
            }
        }
        return created;
    }

    private BigDecimal resolveBedRate(IpdAdmission a) {
        if (a.getBedId() == null) {
            return DEFAULT_BED;
        }
        try {
            AccommodationBedDto bed = accommodationClient.getBed(
                    a.getBedId(), AccommodationClient.headersFor(a.getTenantId(), a.getShopId()));
            if (bed != null && bed.getDailyCharge() != null
                    && bed.getDailyCharge().compareTo(BigDecimal.ZERO) > 0) {
                return bed.getDailyCharge();
            }
        } catch (Exception ex) {
            log.warn("Bed rate lookup failed admission={} bed={}: {}", a.getId(), a.getBedId(), ex.getMessage());
        }
        return DEFAULT_BED;
    }

    private IpdChargeLine buildLine(
            IpdAdmission a, LocalDate chargeDate, String type, String description, BigDecimal amount) {
        IpdChargeLine line = new IpdChargeLine();
        line.setTenantId(a.getTenantId());
        line.setShopId(a.getShopId());
        line.setAdmissionId(a.getId());
        line.setAdmissionNo(a.getAdmissionNo());
        line.setChargeDate(chargeDate);
        line.setChargeType(type);
        line.setDescription(description);
        line.setQuantity(BigDecimal.ONE);
        line.setUnitAmount(amount);
        line.setAmount(amount);
        line.setStatus("POSTED");
        return line;
    }
}
