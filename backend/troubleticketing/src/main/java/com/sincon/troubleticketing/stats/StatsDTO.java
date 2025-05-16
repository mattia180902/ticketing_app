package com.sincon.troubleticketing.stats;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatsDTO {
    
    // General stats
    private Long totalTickets;
    private Long openTickets;
    private Long inProgressTickets;
    private Long resolvedTickets;
    private Long closedTickets;
    
    // Priority stats
    private Long highPriorityCount;
    private Long mediumPriorityCount;
    private Long lowPriorityCount;
    
    // Time stats
    private Duration avgResponseTime;
    private Duration avgResolutionTime;
    
    // Category stats
    private List<CategoryStatsDTO> categoriesStats;
    
    // Agent stats
    private List<AgentStatsDTO> agentStats;
    
    // Time-based stats (for charts)
    private Map<String, Long> ticketsCreatedByMonth;
    private Map<String, Long> ticketsResolvedByMonth;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatsDTO {
        private Long categoryId;
        private String categoryName;
        private Long ticketCount;
        private Double percentageOfTotal;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentStatsDTO {
        private Long userId;
        private String userName;
        private Long assignedTickets;
        private Long resolvedTickets;
        private Duration avgResolutionTime;
    }
}