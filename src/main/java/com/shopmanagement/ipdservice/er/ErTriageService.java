package com.shopmanagement.ipdservice.er;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionService;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class ErTriageService {

    private static final List<String> BOARD = List.of("WAITING", "IN_TREATMENT");

    private final ErTriageRepository triageRepository;
    private final IpdAdmissionService admissionService;

    public ErTriageService(ErTriageRepository triageRepository, IpdAdmissionService admissionService) {
        this.triageRepository = triageRepository;
        this.admissionService = admissionService;
    }

    public List<ErTriage> board() {
        return triageRepository.findByTenantIdAndShopIdAndStatusInOrderByArrivalAtAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), BOARD);
    }

    @Transactional
    public ErTriage register(ErTriage incoming) {
        if (incoming.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        ErTriage row = new ErTriage();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setPatientId(incoming.getPatientId());
        row.setPatientName(incoming.getPatientName());
        row.setAcuity(incoming.getAcuity() == null || incoming.getAcuity().isBlank()
                ? "ESI3" : incoming.getAcuity().trim().toUpperCase(Locale.ROOT));
        row.setChiefComplaint(incoming.getChiefComplaint());
        row.setStatus("WAITING");
        row.setArrivalAt(LocalDateTime.now());
        row.setNotes(incoming.getNotes());
        row.setCreatedBy(TenantContext.currentActor());
        return triageRepository.save(row);
    }

    @Transactional
    public ErTriage advance(Long id, String status) {
        ErTriage row = require(id);
        row.setStatus(status.trim().toUpperCase(Locale.ROOT));
        return triageRepository.save(row);
    }

    @Transactional
    public ErTriage admitToIpd(Long id) {
        ErTriage row = require(id);
        IpdAdmission draft = new IpdAdmission();
        draft.setPatientId(row.getPatientId());
        draft.setPatientName(row.getPatientName());
        draft.setAdmissionReason(row.getChiefComplaint());
        draft.setDiagnosis(row.getChiefComplaint());
        draft.setPriority("ESI1".equalsIgnoreCase(row.getAcuity()) || "ESI2".equalsIgnoreCase(row.getAcuity())
                ? "EMERGENCY" : "URGENT");
        draft.setEmergency(true);
        draft.setNotes("From ER triage #" + row.getId() + " acuity " + row.getAcuity());
        IpdAdmission created = admissionService.create(draft);
        row.setLinkedAdmissionId(created.getId());
        row.setStatus("ADMITTED");
        return triageRepository.save(row);
    }

    private ErTriage require(Long id) {
        return triageRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("ER triage not found"));
    }
}
