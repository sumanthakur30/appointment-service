package com.shopmanagement.ipdservice.pharmacy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class PharmacyFloorService {

    private static final Set<String> CD_TXN = Set.of("ISSUE", "ADMIN", "WASTE", "RETURN", "COUNT");
    private static final Set<String> IMP_TXN = Set.of("ISSUE", "RETURN", "COUNT", "ADJUST");

    private final ControlledDrugRegisterRepository registerRepository;
    private final WardImprestLocationRepository locationRepository;
    private final ImprestParLevelRepository parRepository;
    private final ImprestTxnRepository txnRepository;
    private final boolean controlledEnabled;
    private final boolean imprestEnabled;

    public PharmacyFloorService(
            ControlledDrugRegisterRepository registerRepository,
            WardImprestLocationRepository locationRepository,
            ImprestParLevelRepository parRepository,
            ImprestTxnRepository txnRepository,
            @Value("${ipd.controlled-drugs.enabled:true}") boolean controlledEnabled,
            @Value("${ipd.floor-stock.enabled:true}") boolean imprestEnabled) {
        this.registerRepository = registerRepository;
        this.locationRepository = locationRepository;
        this.parRepository = parRepository;
        this.txnRepository = txnRepository;
        this.controlledEnabled = controlledEnabled;
        this.imprestEnabled = imprestEnabled;
    }

    public Map<String, Object> flags() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("controlledDrugsEnabled", controlledEnabled);
        m.put("floorStockEnabled", imprestEnabled);
        return m;
    }

    public List<ControlledDrugRegisterEntry> listRegister() {
        requireControlled();
        return registerRepository.findByTenantIdAndShopIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public ControlledDrugRegisterEntry postRegister(ControlledDrugRegisterEntry incoming) {
        requireControlled();
        if (incoming.getMedicineName() == null || incoming.getMedicineName().isBlank()) {
            throw new IllegalArgumentException("medicineName is required");
        }
        if (incoming.getTxnType() == null || incoming.getTxnType().isBlank()) {
            throw new IllegalArgumentException("txnType is required");
        }
        String type = incoming.getTxnType().trim().toUpperCase(Locale.ROOT);
        if (!CD_TXN.contains(type)) {
            throw new IllegalArgumentException("txnType must be one of " + CD_TXN);
        }
        if (incoming.getQuantity() == null || incoming.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if ("ADMIN".equals(type) && (incoming.getWitnessId() == null || incoming.getWitnessId().isBlank())) {
            throw new IllegalArgumentException("witnessId is required for ADMIN");
        }
        ControlledDrugRegisterEntry row = new ControlledDrugRegisterEntry();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(incoming.getAdmissionId());
        row.setWardCode(incoming.getWardCode());
        row.setProductId(incoming.getProductId());
        row.setMedicineName(incoming.getMedicineName().trim());
        row.setBatchNo(incoming.getBatchNo());
        row.setTxnType(type);
        row.setQuantity(incoming.getQuantity().setScale(3, RoundingMode.HALF_UP));
        row.setUnit(incoming.getUnit() != null ? incoming.getUnit() : "UNIT");
        row.setNurseId(incoming.getNurseId() != null ? incoming.getNurseId() : TenantContext.currentActor());
        row.setWitnessId(incoming.getWitnessId());
        row.setMarAdministrationId(incoming.getMarAdministrationId());
        row.setNotes(incoming.getNotes());
        row.setRecordedAt(LocalDateTime.now());
        row.setBalanceAfter(incoming.getBalanceAfter());
        return registerRepository.save(row);
    }

    /** Called from MAR when controlled drug is GIVEN. */
    @Transactional
    public void recordMarAdmin(
            Long admissionId,
            Long productId,
            String medicineName,
            BigDecimal qty,
            String nurseId,
            String witnessId,
            Long marAdminId) {
        if (!controlledEnabled) {
            return;
        }
        ControlledDrugRegisterEntry incoming = new ControlledDrugRegisterEntry();
        incoming.setAdmissionId(admissionId);
        incoming.setProductId(productId);
        incoming.setMedicineName(medicineName);
        incoming.setTxnType("ADMIN");
        incoming.setQuantity(qty != null ? qty : BigDecimal.ONE);
        incoming.setNurseId(nurseId);
        incoming.setWitnessId(witnessId);
        incoming.setMarAdministrationId(marAdminId);
        postRegister(incoming);
    }

    public List<WardImprestLocation> listLocations() {
        requireImprest();
        return locationRepository.findByTenantIdAndShopIdAndActiveTrueOrderByWardCodeAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public WardImprestLocation createLocation(WardImprestLocation incoming) {
        requireImprest();
        if (incoming.getWardCode() == null || incoming.getWardCode().isBlank()) {
            throw new IllegalArgumentException("wardCode is required");
        }
        WardImprestLocation row = new WardImprestLocation();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setWardCode(incoming.getWardCode().trim().toUpperCase(Locale.ROOT));
        row.setWardName(incoming.getWardName());
        row.setWarehouseRef(incoming.getWarehouseRef());
        row.setActive(true);
        return locationRepository.save(row);
    }

    public List<ImprestParLevel> listPar(Long locationId) {
        requireImprest();
        requireLocation(locationId);
        return parRepository.findByTenantIdAndShopIdAndImprestLocationIdOrderByMedicineNameAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), locationId);
    }

    @Transactional
    public ImprestParLevel upsertPar(Long locationId, ImprestParLevel incoming) {
        requireImprest();
        requireLocation(locationId);
        if (incoming.getMedicineName() == null || incoming.getMedicineName().isBlank()) {
            throw new IllegalArgumentException("medicineName is required");
        }
        ImprestParLevel row = new ImprestParLevel();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setImprestLocationId(locationId);
        row.setProductId(incoming.getProductId());
        row.setMedicineName(incoming.getMedicineName().trim());
        row.setMinQty(nz(incoming.getMinQty()));
        row.setParQty(nz(incoming.getParQty()));
        row.setMaxQty(incoming.getMaxQty());
        row.setOnHandQty(nz(incoming.getOnHandQty()));
        row.setUnit(incoming.getUnit() != null ? incoming.getUnit() : "UNIT");
        return parRepository.save(row);
    }

    public List<ImprestTxn> listTxns(Long locationId) {
        requireImprest();
        requireLocation(locationId);
        return txnRepository.findByTenantIdAndShopIdAndImprestLocationIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), locationId);
    }

    @Transactional
    public ImprestTxn postTxn(Long locationId, ImprestTxn incoming) {
        requireImprest();
        requireLocation(locationId);
        if (incoming.getMedicineName() == null || incoming.getMedicineName().isBlank()) {
            throw new IllegalArgumentException("medicineName is required");
        }
        String type = incoming.getTxnType() == null ? "" : incoming.getTxnType().trim().toUpperCase(Locale.ROOT);
        if (!IMP_TXN.contains(type)) {
            throw new IllegalArgumentException("txnType must be one of " + IMP_TXN);
        }
        if (incoming.getQuantity() == null || incoming.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        ImprestTxn row = new ImprestTxn();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setImprestLocationId(locationId);
        row.setProductId(incoming.getProductId());
        row.setMedicineName(incoming.getMedicineName().trim());
        row.setTxnType(type);
        row.setQuantity(incoming.getQuantity().setScale(3, RoundingMode.HALF_UP));
        row.setUnit(incoming.getUnit() != null ? incoming.getUnit() : "UNIT");
        row.setReferenceNo(incoming.getReferenceNo());
        row.setNotes(incoming.getNotes());
        row.setRecordedBy(TenantContext.currentActor());
        row.setRecordedAt(LocalDateTime.now());
        ImprestTxn saved = txnRepository.save(row);

        // Update matching par on_hand when present
        List<ImprestParLevel> pars = listPar(locationId);
        for (ImprestParLevel p : pars) {
            if (!p.getMedicineName().equalsIgnoreCase(saved.getMedicineName())) {
                continue;
            }
            BigDecimal onHand = nz(p.getOnHandQty());
            if ("ISSUE".equals(type)) {
                onHand = onHand.add(saved.getQuantity());
            } else if ("RETURN".equals(type)) {
                onHand = onHand.subtract(saved.getQuantity()).max(BigDecimal.ZERO);
            } else if ("COUNT".equals(type) || "ADJUST".equals(type)) {
                onHand = saved.getQuantity();
            }
            p.setOnHandQty(onHand);
            parRepository.save(p);
            break;
        }
        return saved;
    }

    public List<ImprestParLevel> lowStock(Long locationId) {
        return listPar(locationId).stream()
                .filter(p -> nz(p.getOnHandQty()).compareTo(nz(p.getMinQty())) < 0)
                .toList();
    }

    private WardImprestLocation requireLocation(Long id) {
        return locationRepository
                .findByIdAndTenantIdAndShopId(id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Imprest location not found"));
    }

    private void requireControlled() {
        if (!controlledEnabled) {
            throw new IllegalStateException("Controlled-drug register disabled");
        }
    }

    private void requireImprest() {
        if (!imprestEnabled) {
            throw new IllegalStateException("Floor-stock imprest disabled");
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
