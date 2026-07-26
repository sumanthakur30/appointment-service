package com.shopmanagement.ipdservice.clinical;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.shopmanagement.ipdservice.mar.MarAdministration;
import com.shopmanagement.ipdservice.mar.MarAdministrationRepository;
import com.shopmanagement.ipdservice.mar.MarOrder;
import com.shopmanagement.ipdservice.mar.MarOrderRepository;
import com.shopmanagement.ipdservice.nursing.NursingIntakeOutput;
import com.shopmanagement.ipdservice.nursing.NursingIntakeOutputRepository;
import com.shopmanagement.ipdservice.nursing.NursingNote;
import com.shopmanagement.ipdservice.nursing.NursingNoteRepository;
import com.shopmanagement.ipdservice.nursing.NursingVital;
import com.shopmanagement.ipdservice.nursing.NursingVitalRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class IpdAdmissionChartService {

    private final IpdAdmissionRepository admissionRepository;
    private final PatientClinicalProfileService clinicalProfileService;
    private final NursingVitalRepository vitalRepository;
    private final NursingNoteRepository noteRepository;
    private final NursingIntakeOutputRepository ioRepository;
    private final MarOrderRepository marOrderRepository;
    private final MarAdministrationRepository marAdminRepository;
    private final IpdLabClient ipdLabClient;

    public IpdAdmissionChartService(
            IpdAdmissionRepository admissionRepository,
            PatientClinicalProfileService clinicalProfileService,
            NursingVitalRepository vitalRepository,
            NursingNoteRepository noteRepository,
            NursingIntakeOutputRepository ioRepository,
            MarOrderRepository marOrderRepository,
            MarAdministrationRepository marAdminRepository,
            IpdLabClient ipdLabClient) {
        this.admissionRepository = admissionRepository;
        this.clinicalProfileService = clinicalProfileService;
        this.vitalRepository = vitalRepository;
        this.noteRepository = noteRepository;
        this.ioRepository = ioRepository;
        this.marOrderRepository = marOrderRepository;
        this.marAdminRepository = marAdminRepository;
        this.ipdLabClient = ipdLabClient;
    }

    public Map<String, Object> chart(Long admissionId) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        IpdAdmission admission = admissionRepository.findByIdAndTenantIdAndShopId(admissionId, tenantId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));

        List<NursingVital> vitals = vitalRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(tenantId, shopId, admissionId);
        List<NursingNote> notes = noteRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(tenantId, shopId, admissionId);
        List<NursingIntakeOutput> io = ioRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(tenantId, shopId, admissionId);
        List<MarOrder> marOrders = marOrderRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByStartAtDesc(tenantId, shopId, admissionId);
        List<MarAdministration> marAdmin = marAdminRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByAdministeredAtDesc(tenantId, shopId, admissionId);

        List<PatientAllergy> allergies = clinicalProfileService.listAllergies(admission.getPatientId()).stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .toList();
        List<PatientProblem> problems = clinicalProfileService.listProblems(admission.getPatientId()).stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();

        Map<String, Object> labs = ipdLabClient.patientClinicalSummary(admission.getPatientId(), tenantId);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("admission", admission);
        out.put("allergies", allergies);
        out.put("problems", problems);
        out.put("vitals", vitals);
        out.put("notes", notes);
        out.put("intakeOutput", io);
        out.put("marOrders", marOrders);
        out.put("marAdministrations", marAdmin);
        out.put("labOrders", labs.getOrDefault("labOrders", List.of()));
        out.put("labResults", labs.getOrDefault("labResults", List.of()));
        if (!vitals.isEmpty()) {
            out.put("latestNews", com.shopmanagement.ipdservice.nursing.EarlyWarningScore.news2(vitals.get(0)));
            out.put("vitalsTrend", vitals.stream().limit(12).map(v -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("recordedAt", v.getRecordedAt());
                row.put("temperatureC", v.getTemperatureC());
                row.put("pulseBpm", v.getPulseBpm());
                row.put("respRate", v.getRespRate());
                row.put("bpSystolic", v.getBpSystolic());
                row.put("spo2", v.getSpo2());
                row.put("news", com.shopmanagement.ipdservice.nursing.EarlyWarningScore.news2(v));
                return row;
            }).toList());
        } else {
            out.put("latestNews", Map.of("score", 0, "risk", "NONE"));
            out.put("vitalsTrend", List.of());
        }
        return out;
    }

    public Map<String, Object> placeLabOrder(Long admissionId, Map<String, Object> request) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        IpdAdmission admission = admissionRepository.findByIdAndTenantIdAndShopId(admissionId, tenantId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = request.get("items") instanceof List<?> list
                ? (List<Map<String, Object>>) (List<?>) list
                : List.of();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("At least one lab test item is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerId", admission.getPatientId());
        body.put("encounterId", admission.getEncounterId());
        body.put("branchId", request.get("branchId") != null ? request.get("branchId") : 1);
        body.put("sourceType", "IPD");
        body.put("doctorName", request.get("doctorName"));
        body.put("notes", "IPD admission " + admission.getAdmissionNo()
                + (request.get("notes") != null ? (" · " + request.get("notes")) : ""));
        body.put("items", items);
        return ipdLabClient.createLabOrder(body);
    }
}
