package com.sincon.troubleticketing.ticketHistory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sincon.troubleticketing.exception.ResourceNotFoundException;
import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserRepository;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public List<TicketHistoryDTO> getHistoryByTicketId(Long ticketId) {
        return historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketHistoryDTO createTicketHistory(Ticket ticket, String field, String oldValue, String newValue, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setUser(user);
        history.setField(field);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setCreatedAt(LocalDateTime.now());
        
        TicketHistory savedHistory = historyRepository.save(history);
        return convertToDTO(savedHistory);
    }

    private TicketHistoryDTO convertToDTO(TicketHistory history) {
        TicketHistoryDTO dto = new TicketHistoryDTO();
        dto.setId(history.getId());
        dto.setField(history.getField());
        dto.setOldValue(history.getOldValue());
        dto.setNewValue(history.getNewValue());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setTicketId(history.getTicket().getId());
        dto.setUserId(history.getUser().getId());
        
        // Set user info
        dto.setUserName(history.getUser().getFirstName() + " " + history.getUser().getLastName());
        dto.setUserRole(history.getUser().getRole().name());
        
        return dto;
    }
}