package com.beanbrew.controller;

import com.beanbrew.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return analyticsService.overview();
    }

    @GetMapping("/coffee-preferences")
    public Map<String, Object> coffeePreferences() {
        return analyticsService.coffeePreferences();
    }

    @GetMapping("/peak-hours")
    public Map<String, Object> peakHours() {
        return analyticsService.peakHours();
    }

    @GetMapping("/departments")
    public Map<String, Object> departments() {
        return analyticsService.departmentAnalytics();
    }

    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam(defaultValue = "daily") String range) {
        return analyticsService.consumptionTrend(range);
    }
}
