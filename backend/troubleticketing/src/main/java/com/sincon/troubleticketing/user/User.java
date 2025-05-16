package com.sincon.troubleticketing.user;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sincon.troubleticketing.comment.Comment;
import com.sincon.troubleticketing.enums.*;
import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.ticketHistory.TicketHistory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column
    private String department;
    
    @Column
    private String avatarUrl;
    
    @Column(nullable = false)
    private boolean active = true;
    
    // Relations
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<Ticket> createdTickets = new HashSet<>();
    
    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<Ticket> assignedTickets = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<Comment> comments = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<TicketHistory> ticketHistories = new HashSet<>();
    
    // Helper methods for bidirectional relationships
    
    public void addCreatedTicket(Ticket ticket) {
        createdTickets.add(ticket);
        ticket.setCreatedBy(this);
    }
    
    public void removeCreatedTicket(Ticket ticket) {
        createdTickets.remove(ticket);
        ticket.setCreatedBy(null);
    }
    
    public void addAssignedTicket(Ticket ticket) {
        assignedTickets.add(ticket);
        ticket.setAssignedTo(this);
    }
    
    public void removeAssignedTicket(Ticket ticket) {
        assignedTickets.remove(ticket);
        ticket.setAssignedTo(null);
    }
    
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setUser(this);
    }
    
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setUser(null);
    }
    
    public void addTicketHistory(TicketHistory history) {
        ticketHistories.add(history);
        history.setUser(this);
    }
    
    public void removeTicketHistory(TicketHistory history) {
        ticketHistories.remove(history);
        history.setUser(null);
    }
}
