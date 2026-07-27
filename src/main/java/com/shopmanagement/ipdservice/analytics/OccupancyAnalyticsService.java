package com.shopmanagement.ipdservice.analytics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationNodeDto;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class OccupancyAnalyticsService {

    private final AccommodationClient accommodationClient;
    private final IpdAdmissionRepository admissionRepository;

    public OccupancyAnalyticsService(
            AccommodationClient accommodationClient, IpdAdmissionRepository admissionRepository) {
        this.accommodationClient = accommodationClient;
        this.admissionRepository = admissionRepository;
    }

    public Map<String, Object> occupancyHeatmap() {
        List<AccommodationBedDto> beds = accommodationClient.listBeds();
        List<AccommodationNodeDto> nodes = accommodationClient.listNodes();
        Map<Long, AccommodationNodeDto> nodeById = new HashMap<>();
        for (AccommodationNodeDto n : nodes) {
            nodeById.put(n.getId(), n);
        }

        Map<Long, HeatCell> byRoom = new LinkedHashMap<>();
        for (AccommodationBedDto bed : beds) {
            Long roomId = bed.getRoomNodeId();
            HeatCell cell = byRoom.computeIfAbsent(roomId, id -> {
                HeatCell c = new HeatCell();
                c.roomNodeId = id;
                AccommodationNodeDto room = nodeById.get(id);
                c.roomCode = room != null ? room.getCode() : String.valueOf(id);
                c.roomName = room != null ? room.getName() : ("Room " + id);
                c.wardName = resolveAncestorName(room, nodeById, "WARD");
                c.floorName = resolveAncestorName(room, nodeById, "FLOOR");
                return c;
            });
            cell.total++;
            String st = bed.getStatus() == null ? "" : bed.getStatus().toUpperCase();
            switch (st) {
                case "AVAILABLE" -> cell.available++;
                case "OCCUPIED", "ISOLATION", "TRANSFER_PENDING" -> cell.occupied++;
                case "RESERVED", "SURGERY_HOLD", "EMERGENCY_HOLD" -> cell.reserved++;
                case "CLEANING" -> cell.cleaning++;
                case "MAINTENANCE", "BLOCKED" -> cell.blocked++;
                default -> cell.other++;
            }
        }

        List<Map<String, Object>> cells = new ArrayList<>();
        int totalBeds = 0;
        int totalOcc = 0;
        for (HeatCell c : byRoom.values()) {
            totalBeds += c.total;
            totalOcc += c.occupied + c.reserved;
            double pct = c.total == 0 ? 0 : (100.0 * (c.occupied + c.reserved) / c.total);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("roomNodeId", c.roomNodeId);
            row.put("roomCode", c.roomCode);
            row.put("roomName", c.roomName);
            row.put("wardName", c.wardName);
            row.put("floorName", c.floorName);
            row.put("total", c.total);
            row.put("available", c.available);
            row.put("occupied", c.occupied);
            row.put("reserved", c.reserved);
            row.put("cleaning", c.cleaning);
            row.put("blocked", c.blocked);
            row.put("other", c.other);
            row.put("occupancyPct", Math.round(pct * 10) / 10.0);
            row.put("heat", heatBand(pct));
            cells.add(row);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalBeds", totalBeds);
        out.put("occupiedLike", totalOcc);
        out.put("overallOccupancyPct",
                totalBeds == 0 ? 0 : Math.round(1000.0 * totalOcc / totalBeds) / 10.0);
        out.put("cells", cells);
        return out;
    }

    public Map<String, Object> occupancyKpis() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<IpdAdmission> active = admissionRepository.findByTenantIdAndShopIdAndStatusInOrderByAdmittedAtDesc(
                tenantId, shopId, List.of("ADMITTED", "TRANSFERRED"));
        List<IpdAdmission> discharged = admissionRepository.findByTenantIdAndShopIdAndStatusInOrderByAdmittedAtDesc(
                tenantId, shopId, List.of("DISCHARGED"));

        double alosDays = 0;
        int alosN = 0;
        for (IpdAdmission a : discharged) {
            if (a.getAdmittedAt() == null || a.getDischargedAt() == null) {
                continue;
            }
            long hours = Duration.between(a.getAdmittedAt(), a.getDischargedAt()).toHours();
            if (hours < 0) {
                continue;
            }
            alosDays += hours / 24.0;
            alosN++;
        }

        Map<String, Long> byDept = new LinkedHashMap<>();
        for (IpdAdmission a : active) {
            String dept = a.getDepartment() == null || a.getDepartment().isBlank() ? "GENERAL" : a.getDepartment();
            byDept.merge(dept, 1L, Long::sum);
        }

        Map<String, Object> heat = occupancyHeatmap();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("currentCensus", active.size());
        out.put("dischargedCount", discharged.size());
        out.put("alosDays", alosN == 0 ? 0 : Math.round((alosDays / alosN) * 10) / 10.0);
        out.put("alosSampleSize", alosN);
        out.put("censusByDepartment", byDept);
        out.put("totalBeds", heat.get("totalBeds"));
        out.put("occupiedLike", heat.get("occupiedLike"));
        out.put("overallOccupancyPct", heat.get("overallOccupancyPct"));
        out.put("asOf", LocalDateTime.now().toString());
        return out;
    }

    private static String heatBand(double pct) {
        if (pct >= 90) {
            return "CRITICAL";
        }
        if (pct >= 75) {
            return "HIGH";
        }
        if (pct >= 50) {
            return "MEDIUM";
        }
        if (pct > 0) {
            return "LOW";
        }
        return "EMPTY";
    }

    private static String resolveAncestorName(
            AccommodationNodeDto start, Map<Long, AccommodationNodeDto> byId, String nodeType) {
        AccommodationNodeDto cur = start;
        int guard = 0;
        while (cur != null && guard++ < 20) {
            if (nodeType.equalsIgnoreCase(cur.getNodeType())) {
                return cur.getName();
            }
            if (cur.getParentId() == null) {
                break;
            }
            cur = byId.get(cur.getParentId());
        }
        return null;
    }

    private static final class HeatCell {
        Long roomNodeId;
        String roomCode;
        String roomName;
        String wardName;
        String floorName;
        int total;
        int available;
        int occupied;
        int reserved;
        int cleaning;
        int blocked;
        int other;
    }
}
