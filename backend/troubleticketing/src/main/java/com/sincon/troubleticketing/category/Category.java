package com.sincon.troubleticketing.category;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sincon.troubleticketing.ticket.Ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    // Relations
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonBackReference
    private Set<Ticket> tickets = new HashSet<>();
    
    // Helper methods for bidirectional relationships
    
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setCategory(this);
    }
    
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
        ticket.setCategory(null);
    }
}