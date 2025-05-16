package com.sincon.troubleticketing.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotificationsForCurrentUser() {
        List<NotificationDTO> notifications = notificationService.getAllNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationDTO>> getNotificationsForCurrentUserPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationDTO> notifications = notificationService.getNotificationsForCurrentUserPaginated(pageable);
        
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotificationsForCurrentUser() {
        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount() {
        long count = notificationService.getUnreadNotificationCount();
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(@PathVariable Long id) {
        NotificationDTO notification = notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(notification);
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        notificationService.markAllNotificationsAsRead();
        return ResponseEntity.noContent().build();
    }
}
