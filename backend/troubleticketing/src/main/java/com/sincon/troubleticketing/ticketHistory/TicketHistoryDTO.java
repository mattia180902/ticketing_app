package com.sincon.troubleticketing.ticketHistory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketHistoryDTO {
    
    private Long id;
    private String field;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
    
    // Relationships
    private Long ticketId;
    private Long userId;
    
    // Denormalized fields for display
    private String userName;
    private String userRole;
}