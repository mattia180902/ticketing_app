package com.sincon.troubleticketing.stats;

import org.springframework.stereotype.Component;

@Component
public class StatsMapper {

    public StatsDTO toDto(Stats stats) {
        return StatsDTO.builder()
                .totalTickets(stats.getTotalTickets())
                .openTickets(stats.getOpenTickets())
                .closedTickets(stats.getClosedTickets())
                .resolvedTickets(stats.getResolvedTickets())
                .build();
    }
}
