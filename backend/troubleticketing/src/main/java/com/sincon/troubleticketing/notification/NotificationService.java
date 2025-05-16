package com.sincon.troubleticketing.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sincon.troubleticketing.exception.ResourceNotFoundException;
import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    
    public List<NotificationDTO> getAllNotificationsForCurrentUser() {
        User currentUser = getCurrentUser();
        
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public Page<NotificationDTO> getNotificationsForCurrentUserPaginated(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable)
                .map(notificationMapper::toDTO);
    }
    
    public List<NotificationDTO> getUnreadNotificationsForCurrentUser() {
        User currentUser = getCurrentUser();
        
        return notificationRepository.findByUserAndReadOrderByCreatedAtDesc(currentUser, false).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificationDTO markNotificationAsRead(Long id) {
        User currentUser = getCurrentUser();
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        
        // Ensure notification belongs to current user
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Cannot mark notification as read: notification does not belong to current user");
        }
        
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        
        return notificationMapper.toDTO(savedNotification);
    }
    
    @Transactional
    public void markAllNotificationsAsRead() {
        User currentUser = getCurrentUser();
        notificationRepository.markAllAsRead(currentUser.getId());
    }
    
    @Transactional
    public long getUnreadNotificationCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByUserAndRead(currentUser, false);
    }
    
    @Transactional
    public NotificationDTO createTicketAssignedNotification(Ticket ticket, User assignedUser) {
        Notification notification = Notification.builder()
                .message("You have been assigned to ticket #" + ticket.getId() + ": " + ticket.getSubject())
                .type(Notification.NotificationType.INFO)
                .user(assignedUser)
                .ticket(ticket)
                .read(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toDTO(savedNotification);
    }
    
    @Transactional
    public NotificationDTO createTicketStatusChangedNotification(Ticket ticket, User ticketCreator) {
        Notification notification = Notification.builder()
                .message("Ticket #" + ticket.getId() + " status changed to " + ticket.getStatus())
                .type(Notification.NotificationType.INFO)
                .user(ticketCreator)
                .ticket(ticket)
                .read(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toDTO(savedNotification);
    }
    
    @Transactional
    public NotificationDTO createCommentNotification(Ticket ticket, User recipient) {
        Notification notification = Notification.builder()
                .message("New comment added to ticket #" + ticket.getId() + ": " + ticket.getSubject())
                .type(Notification.NotificationType.INFO)
                .user(recipient)
                .ticket(ticket)
                .read(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toDTO(savedNotification);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}