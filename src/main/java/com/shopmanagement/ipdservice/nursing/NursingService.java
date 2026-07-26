package com.shopmanagement.ipdservice.nursing;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class NursingService {

    private static final List<String> ACTIVE = List.of("ADMITTED", "TRANSFERRED");

    private final IpdAdmissionRepository admissionRepository;
    private final NursingVitalRepository vitalRepository;
    private final NursingIntakeOutputRepository ioRepository;
    private final NursingNoteRepository noteRepository;

    public NursingService(
            IpdAdmissionRepository admissionRepository,
            NursingVitalRepository vitalRepository,
            NursingIntakeOutputRepository ioRepository,
            NursingNoteRepository noteRepository) {
        this.admissionRepository = admissionRepository;
        this.vitalRepository = vitalRepository;
        this.ioRepository = ioRepository;
        this.noteRepository = noteRepository;
    }

    public List<IpdAdmission> wardCensus() {
        return admissionRepository.findByTenantIdAndShopIdAndStatusInOrderByAdmittedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), ACTIVE);
    }

    public NursingVital recordVital(Long admissionId, NursingVital incoming) {
        assertActiveAdmission(admissionId);
        NursingVital v = new NursingVital();
        v.setTenantId(TenantContext.requireTenantId());
        v.setShopId(TenantContext.requireShopId());
        v.setAdmissionId(admissionId);
        v.setRecordedAt(incoming.getRecordedAt() != null ? incoming.getRecordedAt() : LocalDateTime.now());
        v.setTemperatureC(incoming.getTemperatureC());
        v.setPulseBpm(incoming.getPulseBpm());
        v.setRespRate(incoming.getRespRate());
        v.setBpSystolic(incoming.getBpSystolic());
        v.setBpDiastolic(incoming.getBpDiastolic());
        v.setSpo2(incoming.getSpo2());
        v.setPainScore(incoming.getPainScore());
        v.setFallRisk(incoming.getFallRisk());
        v.setNotes(incoming.getNotes());
        v.setRecordedBy(TenantContext.currentActor());
        return vitalRepository.save(v);
    }

    public List<NursingVital> listVitals(Long admissionId) {
        assertAdmission(admissionId);
        return vitalRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    public NursingIntakeOutput recordIo(Long admissionId, NursingIntakeOutput incoming) {
        assertActiveAdmission(admissionId);
        if (incoming.getIoType() == null || incoming.getIoType().isBlank()) {
            throw new IllegalArgumentException("ioType is required (INTAKE|OUTPUT)");
        }
        if (incoming.getAmountMl() == null) {
            throw new IllegalArgumentException("amountMl is required");
        }
        NursingIntakeOutput row = new NursingIntakeOutput();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(admissionId);
        row.setRecordedAt(incoming.getRecordedAt() != null ? incoming.getRecordedAt() : LocalDateTime.now());
        row.setIoType(incoming.getIoType().trim().toUpperCase());
        row.setCategory(incoming.getCategory());
        row.setAmountMl(incoming.getAmountMl());
        row.setNotes(incoming.getNotes());
        row.setRecordedBy(TenantContext.currentActor());
        return ioRepository.save(row);
    }

    public List<NursingIntakeOutput> listIo(Long admissionId) {
        assertAdmission(admissionId);
        return ioRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    public NursingNote addNote(Long admissionId, NursingNote incoming) {
        assertActiveAdmission(admissionId);
        if (incoming.getBody() == null || incoming.getBody().isBlank()) {
            throw new IllegalArgumentException("body is required");
        }
        NursingNote n = new NursingNote();
        n.setTenantId(TenantContext.requireTenantId());
        n.setShopId(TenantContext.requireShopId());
        n.setAdmissionId(admissionId);
        n.setNoteType(incoming.getNoteType() == null || incoming.getNoteType().isBlank()
                ? "PROGRESS" : incoming.getNoteType().trim().toUpperCase());
        n.setBody(incoming.getBody().trim());
        n.setAssessmentJson(incoming.getAssessmentJson());
        n.setRecordedAt(incoming.getRecordedAt() != null ? incoming.getRecordedAt() : LocalDateTime.now());
        n.setRecordedBy(TenantContext.currentActor());
        return noteRepository.save(n);
    }

    public List<NursingNote> listNotes(Long admissionId) {
        assertAdmission(admissionId);
        return noteRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private IpdAdmission assertActiveAdmission(Long admissionId) {
        IpdAdmission a = assertAdmission(admissionId);
        if (!ACTIVE.contains(a.getStatus())) {
            throw new IllegalStateException("Admission is not active: " + a.getStatus());
        }
        return a;
    }
}
