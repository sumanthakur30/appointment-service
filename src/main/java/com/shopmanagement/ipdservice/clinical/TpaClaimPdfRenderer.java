package com.shopmanagement.ipdservice.clinical;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

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

/**
 * Template-driven TPA claim PDF — walks form schema sections like discharge summary.
 * Insurer layouts come from form-builder / embedded claim templates (config), not hardcoded school code.
 */
@Component
public class TpaClaimPdfRenderer {

    public byte[] render(IpdAdmission admission, Map<String, Object> formSchema, Map<String, Object> answers) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            String titleText = String.valueOf(formSchema.getOrDefault("title", "TPA / Cashless Claim"));
            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6f);
            doc.add(title);

            Paragraph fmt = new Paragraph(
                    "Format: " + String.valueOf(answers.getOrDefault("claimFormat",
                            formSchema.getOrDefault("claimFormat", "GENERIC"))),
                    mutedFont);
            fmt.setAlignment(Element.ALIGN_CENTER);
            fmt.setSpacingAfter(12f);
            doc.add(fmt);

            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setSpacingAfter(10f);
            addKv(header, "Admission No", admission.getAdmissionNo(), labelFont, bodyFont);
            addKv(header, "Patient",
                    (admission.getPatientName() != null ? admission.getPatientName() : "")
                            + " (#" + admission.getPatientId() + ")",
                    labelFont, bodyFont);
            doc.add(header);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sections = formSchema.get("sections") instanceof List<?> list
                    ? (List<Map<String, Object>>) (List<?>) list
                    : List.of();

            if (sections.isEmpty()) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                for (Map.Entry<String, Object> e : answers.entrySet()) {
                    addKv(table, e.getKey(), String.valueOf(e.getValue()), labelFont, bodyFont);
                }
                doc.add(table);
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
                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);
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
                        addKv(table, label, String.valueOf(val), labelFont, bodyFont);
                    }
                    doc.add(table);
                }
            }

            Paragraph footer = new Paragraph(
                    "Template-driven claim. Override via form-builder key ipd_tpa_claim[_format] without code changes.",
                    mutedFont);
            footer.setSpacingBefore(18f);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to render TPA claim PDF", ex);
        }
    }

    /** Back-compat helper when only flat claim map is available. */
    public byte[] render(IpdAdmission admission, Map<String, Object> claim) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("title", "TPA / Cashless Claim Export");
        schema.put("claimFormat", claim.getOrDefault("claimFormat", "GENERIC"));
        schema.put("sections", List.of());
        return render(admission, schema, claim);
    }

    private static void addKv(PdfPTable table, String key, String value, Font labelFont, Font bodyFont) {
        PdfPCell k = new PdfPCell(new Phrase(key, labelFont));
        k.setBorderWidth(0.5f);
        k.setPadding(6f);
        PdfPCell v = new PdfPCell(new Phrase(value == null || value.isBlank() ? "—" : value, bodyFont));
        v.setBorderWidth(0.5f);
        v.setPadding(6f);
        table.addCell(k);
        table.addCell(v);
    }
}
