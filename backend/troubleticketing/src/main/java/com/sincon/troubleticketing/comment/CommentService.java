package com.sincon.troubleticketing.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sincon.troubleticketing.exception.ResourceNotFoundException;
import com.sincon.troubleticketing.notification.NotificationService;
import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.ticket.TicketRepository;
import com.sincon.troubleticketing.ticketHistory.TicketHistory;
import com.sincon.troubleticketing.ticketHistory.TicketHistoryRepository;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    
    public List<CommentDTO> getCommentsByTicketId(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        // Find ticket
        Ticket ticket = ticketRepository.findById(commentDTO.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", commentDTO.getTicketId()));
        
        // Set user ID
        commentDTO.setUserId(currentUser.getId());
        
        // Create comment
        Comment comment = commentMapper.toEntity(commentDTO);
        Comment savedComment = commentRepository.save(comment);
        
        // Create ticket history
        createTicketHistory(ticket, currentUser, "COMMENT_ADDED");
        
        // Send notifications
        
        // Notify ticket creator if different from commenter
        if (ticket.getCreatedBy() != null && !ticket.getCreatedBy().equals(currentUser)) {
            notificationService.createCommentNotification(ticket, ticket.getCreatedBy());
        }
        
        // Notify assignee if different from commenter and ticket creator
        if (ticket.getAssignedTo() != null && 
                !ticket.getAssignedTo().equals(currentUser) && 
                !ticket.getAssignedTo().equals(ticket.getCreatedBy())) {
            notificationService.createCommentNotification(ticket, ticket.getAssignedTo());
        }
        
        return commentMapper.toDTO(savedComment);
    }
    
    @Transactional
    public void deleteComment(Long id) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        
        // Check if user is the comment author or has admin rights
        if (!comment.getUser().equals(currentUser) && 
                !currentUser.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("You are not authorized to delete this comment");
        }
        
        // Create ticket history before deleting the comment
        createTicketHistory(comment.getTicket(), currentUser, "COMMENT_DELETED");
        
        commentRepository.delete(comment);
    }
    
    private void createTicketHistory(Ticket ticket, User user, String action) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .user(user)
                //.action(action)
                .build();
        
        ticketHistoryRepository.save(history);
    }
}