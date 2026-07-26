package com.shopmanagement.ipdservice.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationNodeDto;

@Service
public class OccupancyAnalyticsService {

    private final AccommodationClient accommodationClient;

    public OccupancyAnalyticsService(AccommodationClient accommodationClient) {
        this.accommodationClient = accommodationClient;
    }

    /**
     * Heat map by room / ward node: total beds, occupied-like, available, occupancy %.
     */
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
