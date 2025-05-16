package com.sincon.troubleticketing.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserRepository;

@Component
@RequiredArgsConstructor
public class UserSecurity {
    
    private final UserRepository userRepository;
    
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }
    
    public boolean isTicketCreator(Long ticketId, Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username).orElse(null);
        
        if (currentUser == null) {
            return false;
        }
        
        return currentUser.getCreatedTickets().stream()
                .anyMatch(ticket -> ticket.getId().equals(ticketId));
    }
    
    public boolean isTicketAssignee(Long ticketId, Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username).orElse(null);
        
        if (currentUser == null) {
            return false;
        }
        
        return currentUser.getAssignedTickets().stream()
                .anyMatch(ticket -> ticket.getId().equals(ticketId));
    }
}
