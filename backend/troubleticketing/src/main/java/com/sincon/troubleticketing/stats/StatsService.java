package com.sincon.troubleticketing.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sincon.troubleticketing.ticket.TicketRepository;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TicketRepository ticketRepository;

    public StatsDTO getOverallStats() {
        // Esempio base di implementazione
        long totalTickets = ticketRepository.count();
        long openTickets = ticketRepository.countByStatus("OPEN");
        long closedTickets = ticketRepository.countByStatus("CLOSED");

        return StatsDTO.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .closedTickets(closedTickets)
                .build();
    }

    public StatsDTO getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        long totalTickets = ticketRepository.countByCreationDateBetween(startDate, endDate);
        long openTickets = ticketRepository.countByStatusAndCreationDateBetween("OPEN", startDate, endDate);
        long closedTickets = ticketRepository.countByStatusAndCreationDateBetween("CLOSED", startDate, endDate);

        return StatsDTO.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .closedTickets(closedTickets)
                .build();
    }

    public Map<String, Long> getTicketCountByCategory() {
        return ticketRepository.countTicketsByCategory();
    }

    public Map<String, Long> getTicketCountByStatus() {
        return ticketRepository.countTicketsByStatus();
    }

    public Map<String, Long> getTicketCountByPriority() {
        return ticketRepository.countTicketsByPriority();
    }

    public Map<String, Long> getTicketCountByAgent() {
        return ticketRepository.countTicketsByAgent();
    }

    public Map<String, Long> getTicketCountByMonth(Integer months) {
        // es: ultimi X mesi
        return ticketRepository.countTicketsByMonth(months);
    }

    public Map<String, Double> getAverageResponseTimes() {
        return ticketRepository.calculateAverageResponseTimes();
    }

    public Map<String, Double> getAverageResolutionTimes() {
        return ticketRepository.calculateAverageResolutionTimes();
    }
}
