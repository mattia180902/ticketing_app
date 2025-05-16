package com.sincon.troubleticketing.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByUserIdAndReadFalse(Long userId);
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndReadFalse(Long userId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false")
    Long countUnreadByUserId(Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markAsRead(Long id);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllAsRead(Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.ticketId = :ticketId")
    void markTicketNotificationsAsRead(Long userId, Long ticketId);
}
