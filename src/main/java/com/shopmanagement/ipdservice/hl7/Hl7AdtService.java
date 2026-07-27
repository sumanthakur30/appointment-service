package com.shopmanagement.ipdservice.hl7;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.support.TenantContext;

/**
 * Config-driven HL7 v2 ADT stub — queues pipe-delimited messages for outbound feed.
 * Enable with {@code ipd.hl7.adt.enabled}; optional {@code ipd.hl7.adt.endpoint} for future push.
 */
@Service
public class Hl7AdtService {

    private static final Logger log = LoggerFactory.getLogger(Hl7AdtService.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Hl7MessageRepository repository;
    private final boolean enabled;
    private final String endpoint;

    public Hl7AdtService(
            Hl7MessageRepository repository,
            @Value("${ipd.hl7.adt.enabled:true}") boolean enabled,
            @Value("${ipd.hl7.adt.endpoint:}") String endpoint) {
        this.repository = repository;
        this.enabled = enabled;
        this.endpoint = endpoint == null ? "" : endpoint.trim();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Hl7Message> list() {
        return repository.findByTenantIdAndShopIdOrderByCreatedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public Hl7Message emitAdmit(IpdAdmission a) {
        return queue(a, "ADT", "A01");
    }

    @Transactional
    public Hl7Message emitTransfer(IpdAdmission a) {
        return queue(a, "ADT", "A02");
    }

    @Transactional
    public Hl7Message emitDischarge(IpdAdmission a) {
        return queue(a, "ADT", "A03");
    }

    @Transactional
    public Hl7Message emitUpdate(IpdAdmission a) {
        return queue(a, "ADT", "A08");
    }

    private Hl7Message queue(IpdAdmission a, String type, String event) {
        if (!enabled || a == null) {
            return null;
        }
        String controlId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String now = LocalDateTime.now().format(TS);
        String payload = buildAdt(a, type, event, controlId, now);

        Hl7Message msg = new Hl7Message();
        msg.setTenantId(a.getTenantId() != null ? a.getTenantId() : TenantContext.requireTenantId());
        msg.setShopId(a.getShopId() != null ? a.getShopId() : TenantContext.requireShopId());
        msg.setAdmissionId(a.getId());
        msg.setMessageType(type);
        msg.setTriggerEvent(event);
        msg.setControlId(controlId);
        msg.setPayload(payload);
        msg.setEndpoint(endpoint.isBlank() ? null : endpoint);
        if (endpoint.isBlank()) {
            msg.setStatus("QUEUED");
        } else {
            // Stub: mark SENT without actual MLLP push (wire adapter later via config)
            msg.setStatus("SENT");
            msg.setSentAt(LocalDateTime.now());
            log.info("HL7 ADT^{} queued/sent controlId={} admission={}", event, controlId, a.getAdmissionNo());
        }
        return repository.save(msg);
    }

    private static String buildAdt(IpdAdmission a, String type, String event, String controlId, String ts) {
        String msh = String.join("|",
                "MSH", "^~\\&", "SUGAMFLOW", "HOSP", "RECEIVER", "DEST",
                ts, "", type + "^" + event, controlId, "P", "2.5");
        String evn = String.join("|", "EVN", event, ts);
        String pid = String.join("|",
                "PID", "1",
                String.valueOf(a.getPatientId()),
                "",
                "",
                safe(a.getPatientName()) + "^^^^",
                "", "", "", "", "", "", "", "", "", "");
        String pv1 = String.join("|",
                "PV1", "1", "I",
                a.getBedId() != null ? String.valueOf(a.getBedId()) : "",
                "", "", "", "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "", "", "",
                safe(a.getAdmissionNo()),
                "", "", "", "", "", "", "", "", "", "", "", "", "",
                a.getAdmittedAt() != null ? a.getAdmittedAt().format(TS) : "",
                a.getDischargedAt() != null ? a.getDischargedAt().format(TS) : "");
        String dg1 = String.join("|",
                "DG1", "1", "",
                safe(a.getPrimaryIcdCode()) + "^" + safe(a.getPrimaryIcdDesc()) + "^ICD10",
                safe(a.getDiagnosis()),
                "", "A");
        return String.join("\r", msh, evn, pid, pv1, dg1);
    }

    private static String safe(String v) {
        return v == null ? "" : v.replace("|", " ").replace("^", " ").replace("\r", " ").replace("\n", " ");
    }
}
