package com.shopmanagement.ipdservice.forms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InsurerClaimPublishService {

    private final FormDefinitionClient formDefinitionClient;
    private final String tpaClaimFormKey;

    public InsurerClaimPublishService(
            FormDefinitionClient formDefinitionClient,
            @Value("${ipd.forms.tpa-claim-key:ipd_tpa_claim}") String tpaClaimFormKey) {
        this.formDefinitionClient = formDefinitionClient;
        this.tpaClaimFormKey = tpaClaimFormKey;
    }

    public Map<String, Object> publishPack() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(publishOne(tpaClaimFormKey, FormDefinitionClient.genericTpaClaim("GENERIC")));
        results.add(publishOne(tpaClaimFormKey + "_star_health", FormDefinitionClient.starHealthClaim()));
        results.add(publishOne(tpaClaimFormKey + "_niva_bupa", FormDefinitionClient.nivaBupaClaim()));

        long ok = results.stream().filter(r -> Boolean.TRUE.equals(r.get("published"))).count();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("publishedCount", ok);
        out.put("total", results.size());
        out.put("forms", results);
        out.put("message", ok == 0
                ? "Form-builder unreachable — embedded templates remain active"
                : "Published " + ok + " insurer claim template(s) to form-builder");
        return out;
    }

    private Map<String, Object> publishOne(String key, Map<String, Object> form) {
        boolean published = formDefinitionClient.publishForm(key, form);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("formKey", key);
        row.put("title", form.get("title"));
        row.put("claimFormat", form.get("claimFormat"));
        row.put("published", published);
        row.put("source", published ? "FORM_BUILDER" : "EMBEDDED");
        return row;
    }
}
