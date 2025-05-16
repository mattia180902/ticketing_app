package com.sincon.troubleticketing.notification;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private boolean read = false; //non so se serve final
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    @JsonBackReference
    private Ticket ticket;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
