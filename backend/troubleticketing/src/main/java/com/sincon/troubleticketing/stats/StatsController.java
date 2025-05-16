package com.sincon.troubleticketing.stats;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsDTO> getOverallStats() {
        return ResponseEntity.ok(statsService.getOverallStats());
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<StatsDTO> getStatsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statsService.getStatsByDateRange(startDate, endDate));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Map<String, Long>> getTicketCountByCategory() {
        return ResponseEntity.ok(statsService.getTicketCountByCategory());
    }

    @GetMapping("/by-status")
    public ResponseEntity<Map<String, Long>> getTicketCountByStatus() {
        return ResponseEntity.ok(statsService.getTicketCountByStatus());
    }

    @GetMapping("/by-priority")
    public ResponseEntity<Map<String, Long>> getTicketCountByPriority() {
        return ResponseEntity.ok(statsService.getTicketCountByPriority());
    }

    @GetMapping("/by-agent")
    public ResponseEntity<Map<String, Long>> getTicketCountByAgent() {
        return ResponseEntity.ok(statsService.getTicketCountByAgent());
    }

    @GetMapping("/by-month")
    public ResponseEntity<Map<String, Long>> getTicketCountByMonth(
            @RequestParam(defaultValue = "6") Integer months) {
        return ResponseEntity.ok(statsService.getTicketCountByMonth(months));
    }

    @GetMapping("/response-times")
    public ResponseEntity<Map<String, Double>> getAverageResponseTimes() {
        return ResponseEntity.ok(statsService.getAverageResponseTimes());
    }

    @GetMapping("/resolution-times")
    public ResponseEntity<Map<String, Double>> getAverageResolutionTimes() {
        return ResponseEntity.ok(statsService.getAverageResolutionTimes());
    }
}