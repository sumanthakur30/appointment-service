package com.shopmanagement.ipdservice.radiology;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class RadiologyOrderService {

    private static final Set<String> STATUSES =
            Set.of("ORDERED", "SCHEDULED", "IN_PROGRESS", "REPORTED", "CANCELLED");

    private final RadiologyOrderRepository orderRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final boolean dicomLinkEnabled;
    private final String viewerUrlTemplate;

    public RadiologyOrderService(
            RadiologyOrderRepository orderRepository,
            IpdAdmissionRepository admissionRepository,
            @Value("${ipd.radiology.dicom-link-enabled:true}") boolean dicomLinkEnabled,
            @Value("${ipd.radiology.dicom-viewer-url-template:https://pacs.local/viewer?study={studyUid}}")
                    String viewerUrlTemplate) {
        this.orderRepository = orderRepository;
        this.admissionRepository = admissionRepository;
        this.dicomLinkEnabled = dicomLinkEnabled;
        this.viewerUrlTemplate = viewerUrlTemplate;
    }

    public List<RadiologyOrder> listForAdmission(Long admissionId) {
        assertAdmission(admissionId);
        List<RadiologyOrder> rows = orderRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByOrderedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
        rows.forEach(this::enrichViewerUrl);
        return rows;
    }

    @Transactional
    public RadiologyOrder create(Long admissionId, RadiologyOrder incoming) {
        IpdAdmission admission = assertAdmission(admissionId);
        if (incoming.getStudyCode() == null || incoming.getStudyCode().isBlank()
                || incoming.getStudyName() == null || incoming.getStudyName().isBlank()) {
            throw new IllegalArgumentException("studyCode and studyName are required");
        }
        RadiologyOrder row = new RadiologyOrder();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(admissionId);
        row.setPatientId(admission.getPatientId());
        row.setEncounterId(admission.getEncounterId());
        row.setModality(incoming.getModality() == null || incoming.getModality().isBlank()
                ? "XRAY" : incoming.getModality().trim().toUpperCase(Locale.ROOT));
        row.setStudyCode(incoming.getStudyCode().trim());
        row.setStudyName(incoming.getStudyName().trim());
        row.setClinicalIndication(incoming.getClinicalIndication());
        row.setStatus("ORDERED");
        row.setOrderedAt(LocalDateTime.now());
        row.setCreatedBy(TenantContext.currentActor());
        row.setAccessionNo(incoming.getAccessionNo());
        row.setStudyInstanceUid(incoming.getStudyInstanceUid());
        row.setPacsUrl(incoming.getPacsUrl());
        return orderRepository.save(row);
    }

    @Transactional
    public RadiologyOrder advance(Long id, String status, String reportText) {
        RadiologyOrder row = orderRepository
                .findByIdAndTenantIdAndShopId(id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Radiology order not found"));
        String st = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(st)) {
            throw new IllegalArgumentException("Invalid status; use " + STATUSES);
        }
        row.setStatus(st);
        if ("REPORTED".equals(st)) {
            row.setReportedAt(LocalDateTime.now());
            if (reportText != null && !reportText.isBlank()) {
                row.setReportText(reportText.trim());
            }
            if (dicomLinkEnabled && (row.getStudyInstanceUid() == null || row.getStudyInstanceUid().isBlank())) {
                row.setStudyInstanceUid("1.2.840.demo." + UUID.randomUUID().toString().replace("-", ""));
            }
            if (dicomLinkEnabled && (row.getAccessionNo() == null || row.getAccessionNo().isBlank())) {
                row.setAccessionNo("ACC-" + row.getId());
            }
            enrichViewerUrl(row);
        }
        return orderRepository.save(row);
    }

    private void enrichViewerUrl(RadiologyOrder row) {
        if (!dicomLinkEnabled) {
            return;
        }
        if (row.getPacsUrl() != null && !row.getPacsUrl().isBlank()) {
            return;
        }
        if (row.getStudyInstanceUid() == null || row.getStudyInstanceUid().isBlank()) {
            return;
        }
        row.setPacsUrl(viewerUrlTemplate.replace("{studyUid}", row.getStudyInstanceUid())
                .replace("{accession}", row.getAccessionNo() == null ? "" : row.getAccessionNo()));
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }
}
