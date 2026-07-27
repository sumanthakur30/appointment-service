package com.shopmanagement.ipdservice.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.analytics.OccupancyAnalyticsService;

@RestController
@RequestMapping("/ipd/analytics")
public class AnalyticsController {

    private final OccupancyAnalyticsService occupancyAnalyticsService;

    public AnalyticsController(OccupancyAnalyticsService occupancyAnalyticsService) {
        this.occupancyAnalyticsService = occupancyAnalyticsService;
    }

    @GetMapping("/occupancy-heatmap")
    public Map<String, Object> occupancyHeatmap() {
        return occupancyAnalyticsService.occupancyHeatmap();
    }

    @GetMapping("/occupancy-kpis")
    public Map<String, Object> occupancyKpis() {
        return occupancyAnalyticsService.occupancyKpis();
    }
}
