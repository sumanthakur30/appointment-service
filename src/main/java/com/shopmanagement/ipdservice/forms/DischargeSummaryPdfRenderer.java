package com.shopmanagement.ipdservice.forms;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;

@Component
public class DischargeSummaryPdfRenderer {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final ObjectMapper objectMapper;

    public DischargeSummaryPdfRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] render(IpdAdmission admission, IpdFormSubmission submission) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph(
                    submission.getFormTitle() != null ? submission.getFormTitle() : "Discharge Summary",
                    titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12f);
            doc.add(title);

            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setSpacingAfter(14f);
            addKv(header, "Admission No", admission.getAdmissionNo(), labelFont, bodyFont);
            addKv(header, "Patient",
                    (admission.getPatientName() != null ? admission.getPatientName() : "")
                            + " (#" + admission.getPatientId() + ")",
                    labelFont, bodyFont);
            addKv(header, "Department", blank(admission.getDepartment()), labelFont, bodyFont);
            addKv(header, "Diagnosis on admit", blank(admission.getDiagnosis()), labelFont, bodyFont);
            addKv(header, "Primary ICD",
                    blank(admission.getPrimaryIcdCode())
                            + (admission.getPrimaryIcdDesc() != null && !admission.getPrimaryIcdDesc().isBlank()
                                    ? " — " + admission.getPrimaryIcdDesc() : ""),
                    labelFont, bodyFont);
            addKv(header, "Secondary ICD", blank(admission.getSecondaryIcdCodes()), labelFont, bodyFont);
            addKv(header, "Admitted", format(admission.getAdmittedAt()), labelFont, bodyFont);
            addKv(header, "Discharged", format(admission.getDischargedAt()), labelFont, bodyFont);
            doc.add(header);

            Map<String, Object> answers = parseAnswers(submission.getAnswersJson());
            Map<String, Object> schema = parseAnswers(submission.getSchemaJson());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sections = schema.get("sections") instanceof List<?> list
                    ? (List<Map<String, Object>>) (List<?>) list
                    : List.of();

            if (sections.isEmpty()) {
                for (Map.Entry<String, Object> e : answers.entrySet()) {
                    doc.add(sectionLine(e.getKey(), String.valueOf(e.getValue()), labelFont, bodyFont));
                }
            } else {
                for (Map<String, Object> section : sections) {
                    String sectionTitle = String.valueOf(section.getOrDefault("title", "Section"));
                    Paragraph h = new Paragraph(sectionTitle, labelFont);
                    h.setSpacingBefore(8f);
                    h.setSpacingAfter(4f);
                    doc.add(h);
                    Object fieldsObj = section.get("fields");
                    if (!(fieldsObj instanceof List<?> fields)) {
                        continue;
                    }
                    for (Object fieldObj : fields) {
                        if (!(fieldObj instanceof Map<?, ?> field)) {
                            continue;
                        }
                        String key = String.valueOf(field.get("key"));
                        Object labelObj = field.get("label");
                        String label = labelObj != null ? String.valueOf(labelObj) : key;
                        Object val = answers.get(key);
                        if (val == null || String.valueOf(val).isBlank()) {
                            continue;
                        }
                        doc.add(sectionLine(label, String.valueOf(val), labelFont, bodyFont));
                    }
                }
            }

            Paragraph foot = new Paragraph(
                    "Submitted by " + blank(submission.getSubmittedBy())
                            + " at " + format(submission.getSubmittedAt())
                            + " · form " + blank(submission.getFormKey()),
                    mutedFont);
            foot.setSpacingBefore(18f);
            doc.add(foot);

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not render discharge summary PDF: " + ex.getMessage(), ex);
        }
    }

    private Paragraph sectionLine(String label, String value, Font labelFont, Font bodyFont) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label + ": ", labelFont));
        p.add(new Phrase(value, bodyFont));
        p.setSpacingAfter(4f);
        return p;
    }

    private void addKv(PdfPTable table, String k, String v, Font labelFont, Font bodyFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, labelFont));
        PdfPCell c2 = new PdfPCell(new Phrase(v, bodyFont));
        c1.setBorderWidth(0);
        c2.setBorderWidth(0);
        c1.setPadding(3f);
        c2.setPadding(3f);
        table.addCell(c1);
        table.addCell(c2);
    }

    private Map<String, Object> parseAnswers(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String format(java.time.LocalDateTime value) {
        return value == null ? "—" : DT.format(value);
    }
}
