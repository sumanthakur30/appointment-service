package com.shopmanagement.ipdservice.accommodation.client;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationNodeDto;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.BedOccupancyDto;
import com.shopmanagement.ipdservice.filter.RequestIdFilter;
import com.shopmanagement.security.SecurityHeaderNames;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Remote bed/occupancy operations against accommodation-service (Phase 4.1).
 */
@Component
public class AccommodationClient {

    private static final Logger log = LoggerFactory.getLogger(AccommodationClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final WebClient webClient;
    private final boolean enabled;
    private final String internalApiKey;

    public AccommodationClient(
            WebClient.Builder builder,
            @Value("${accommodation.service.base-url:http://localhost:8101}") String baseUrl,
            @Value("${accommodation.service.enabled:true}") boolean enabled,
            @Value("${security.jwt.internal-api-key:${SECURITY_INTERNAL_API_KEY:${SECURITY_INVITE_INTERNAL_KEY:}}}")
                    String internalApiKey) {
        this.webClient = builder.baseUrl(trimSlash(baseUrl)).build();
        this.enabled = enabled;
        this.internalApiKey = internalApiKey == null ? "" : internalApiKey.trim();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BedOccupancyDto allocateBed(
            Long bedId,
            String admissionNo,
            String occupantRef,
            String occupantName,
            LocalDateTime expectedDischargeAt,
            boolean reserveOnly) {
        return allocateBed(bedId, admissionNo, occupantRef, occupantName, expectedDischargeAt, reserveOnly,
                null, null, currentHeaders());
    }

    public BedOccupancyDto allocateBed(
            Long bedId,
            String admissionNo,
            String occupantRef,
            String occupantName,
            LocalDateTime expectedDischargeAt,
            boolean reserveOnly,
            String patientGender,
            Integer patientAgeYears) {
        return allocateBed(bedId, admissionNo, occupantRef, occupantName, expectedDischargeAt, reserveOnly,
                patientGender, patientAgeYears, currentHeaders());
    }

    public BedOccupancyDto allocateBed(
            Long bedId,
            String admissionNo,
            String occupantRef,
            String occupantName,
            LocalDateTime expectedDischargeAt,
            boolean reserveOnly,
            TenantHeaders headers) {
        return allocateBed(bedId, admissionNo, occupantRef, occupantName, expectedDischargeAt, reserveOnly,
                null, null, headers);
    }

    public BedOccupancyDto allocateBed(
            Long bedId,
            String admissionNo,
            String occupantRef,
            String occupantName,
            LocalDateTime expectedDischargeAt,
            boolean reserveOnly,
            String patientGender,
            Integer patientAgeYears,
            TenantHeaders headers) {
        requireEnabled();
        java.util.HashMap<String, Object> payload = new java.util.HashMap<>();
        payload.put("admissionNo", admissionNo != null ? admissionNo : "");
        payload.put("occupantRef", occupantRef != null ? occupantRef : "");
        payload.put("occupantName", occupantName != null ? occupantName : "");
        payload.put("reserveOnly", reserveOnly);
        if (expectedDischargeAt != null) {
            payload.put("expectedDischargeAt", expectedDischargeAt);
        }
        if (patientGender != null && !patientGender.isBlank()) {
            payload.put("patientGender", patientGender);
        }
        if (patientAgeYears != null) {
            payload.put("patientAgeYears", patientAgeYears);
        }
        try {
            BedOccupancyDto occ = webClient.post()
                    .uri("/accommodation/beds/{id}/allocate", bedId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> applyAuth(h, headers))
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(BedOccupancyDto.class)
                    .timeout(TIMEOUT)
                    .block();
            if (occ == null || occ.getId() == null) {
                throw new IllegalStateException("Accommodation allocate returned empty occupancy");
            }
            return occ;
        } catch (WebClientResponseException ex) {
            log.warn("allocate bed={} failed status={} body={}", bedId, ex.getStatusCode().value(),
                    ex.getResponseBodyAsString());
            throw new IllegalStateException("Bed allocate failed: " + extractMessage(ex), ex);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Bed allocate failed: " + ex.getMessage(), ex);
        }
    }

    public List<AccommodationBedDto> listEligibleBeds(
            String gender,
            Integer ageYears,
            boolean availableOnly,
            Boolean isolationCapable,
            String isolationType) {
        return listEligibleBeds(gender, ageYears, availableOnly, isolationCapable, isolationType, currentHeaders());
    }

    public List<AccommodationBedDto> listEligibleBeds(
            String gender,
            Integer ageYears,
            boolean availableOnly,
            Boolean isolationCapable,
            String isolationType,
            TenantHeaders headers) {
        requireEnabled();
        try {
            List<AccommodationBedDto> beds = webClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/accommodation/beds/eligible")
                                .queryParam("availableOnly", availableOnly);
                        if (gender != null && !gender.isBlank()) {
                            b = b.queryParam("gender", gender);
                        }
                        if (ageYears != null) {
                            b = b.queryParam("ageYears", ageYears);
                        }
                        if (isolationCapable != null) {
                            b = b.queryParam("isolationCapable", isolationCapable);
                        }
                        if (isolationType != null && !isolationType.isBlank()) {
                            b = b.queryParam("isolationType", isolationType);
                        }
                        return b.build();
                    })
                    .headers(h -> applyAuth(h, headers))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AccommodationBedDto>>() {})
                    .timeout(TIMEOUT)
                    .block();
            return beds != null ? beds : List.of();
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("Eligible beds failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Eligible beds failed: " + ex.getMessage(), ex);
        }
    }

    public BedOccupancyDto releaseOccupancy(Long occupancyId) {
        return releaseOccupancy(occupancyId, currentHeaders());
    }

    public BedOccupancyDto releaseOccupancy(Long occupancyId, TenantHeaders headers) {
        requireEnabled();
        try {
            return webClient.post()
                    .uri("/accommodation/occupancies/{id}/release", occupancyId)
                    .headers(h -> applyAuth(h, headers))
                    .retrieve()
                    .bodyToMono(BedOccupancyDto.class)
                    .timeout(TIMEOUT)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("release occupancy={} failed status={} body={}", occupancyId, ex.getStatusCode().value(),
                    ex.getResponseBodyAsString());
            throw new IllegalStateException("Occupancy release failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Occupancy release failed: " + ex.getMessage(), ex);
        }
    }

    public AccommodationBedDto setBedStatus(Long bedId, String status) {
        return setBedStatus(bedId, status, currentHeaders());
    }

    public AccommodationBedDto setBedStatus(Long bedId, String status, TenantHeaders headers) {
        requireEnabled();
        try {
            return webClient.post()
                    .uri("/accommodation/beds/{id}/status", bedId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> applyAuth(h, headers))
                    .bodyValue(Map.of("status", status))
                    .retrieve()
                    .bodyToMono(AccommodationBedDto.class)
                    .timeout(TIMEOUT)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("setBedStatus bed={} status={} failed: {}", bedId, status, ex.getStatusCode().value());
            throw new IllegalStateException("Bed status update failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Bed status update failed: " + ex.getMessage(), ex);
        }
    }

    public AccommodationBedDto getBed(Long bedId) {
        return getBed(bedId, currentHeaders());
    }

    public AccommodationBedDto getBed(Long bedId, TenantHeaders headers) {
        requireEnabled();
        try {
            return webClient.get()
                    .uri("/accommodation/beds/{id}", bedId)
                    .headers(h -> applyAuth(h, headers))
                    .retrieve()
                    .bodyToMono(AccommodationBedDto.class)
                    .timeout(TIMEOUT)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            return null;
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("Get bed failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Get bed failed: " + ex.getMessage(), ex);
        }
    }

    public List<AccommodationBedDto> listBeds() {
        return listBeds(currentHeaders());
    }

    public List<AccommodationBedDto> listBeds(TenantHeaders headers) {
        requireEnabled();
        try {
            List<AccommodationBedDto> beds = webClient.get()
                    .uri("/accommodation/beds")
                    .headers(h -> applyAuth(h, headers))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AccommodationBedDto>>() {})
                    .timeout(TIMEOUT)
                    .block();
            return beds != null ? beds : List.of();
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("List beds failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("List beds failed: " + ex.getMessage(), ex);
        }
    }

    public List<AccommodationNodeDto> listNodes() {
        return listNodes(currentHeaders());
    }

    public List<AccommodationNodeDto> listNodes(TenantHeaders headers) {
        requireEnabled();
        try {
            List<AccommodationNodeDto> nodes = webClient.get()
                    .uri("/accommodation/nodes")
                    .headers(h -> applyAuth(h, headers))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AccommodationNodeDto>>() {})
                    .timeout(TIMEOUT)
                    .block();
            return nodes != null ? nodes : List.of();
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("List nodes failed: " + extractMessage(ex), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("List nodes failed: " + ex.getMessage(), ex);
        }
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("accommodation-service client is disabled");
        }
    }

    private static TenantHeaders currentHeaders() {
        Long tenantId = RequestIdFilter.getCurrentTenantId();
        String shopId = RequestIdFilter.getCurrentShopId();
        if (tenantId == null || shopId == null || shopId.isBlank()) {
            throw new IllegalStateException("Missing tenant context for accommodation call");
        }
        return new TenantHeaders(
                tenantId,
                shopId,
                RequestIdFilter.getCurrentRole(),
                RequestIdFilter.getCurrentUser());
    }

    public static TenantHeaders headersFor(Long tenantId, String shopId) {
        return new TenantHeaders(tenantId, shopId, "SHOP_OWNER", "ipd-service");
    }

    private static void applyTenant(org.springframework.http.HttpHeaders h, TenantHeaders headers) {
        h.set(RequestIdFilter.TENANT_ID_HEADER, String.valueOf(headers.tenantId()));
        h.set(RequestIdFilter.SHOP_ID_HEADER, headers.shopId());
        h.set(RequestIdFilter.AUTH_ROLE_HEADER,
                headers.role() != null ? headers.role() : "SHOP_OWNER");
        h.set(RequestIdFilter.AUTH_USER_HEADER,
                headers.user() != null ? headers.user() : "ipd-service");
        // Prefer inbound user JWT when present; otherwise trusted service key.
        String bearer = currentAuthorizationHeader();
        if (bearer != null && !bearer.isBlank()) {
            h.set(HttpHeaders.AUTHORIZATION, bearer);
        }
    }

    private void applyAuth(org.springframework.http.HttpHeaders h, TenantHeaders headers) {
        applyTenant(h, headers);
        if (!internalApiKey.isBlank()) {
            h.set(SecurityHeaderNames.INTERNAL_API_KEY, internalApiKey);
        }
    }

    private static String currentAuthorizationHeader() {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest req = servletAttrs.getRequest();
                String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
                    return auth;
                }
            }
        } catch (Exception ignored) {
            // no request context (async / scheduled)
        }
        return null;
    }

    private static String extractMessage(WebClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            return ex.getStatusCode().value() + " " + body;
        }
        return String.valueOf(ex.getStatusCode().value());
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8101";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public record TenantHeaders(Long tenantId, String shopId, String role, String user) {}
}
