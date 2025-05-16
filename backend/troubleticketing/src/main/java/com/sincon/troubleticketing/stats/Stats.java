package com.sincon.troubleticketing.stats;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Long totalTickets;
    private Long openTickets;
    private Long closedTickets;
    private Long resolvedTickets;
    private Long pendingTickets;

    private Double averageResponseTime;
    private Double averageResolutionTime;
}
