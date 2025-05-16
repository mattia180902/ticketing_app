package com.sincon.troubleticketing.ticket;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sincon.troubleticketing.attachment.Attachment;
import com.sincon.troubleticketing.category.Category;
import com.sincon.troubleticketing.comment.Comment;
import com.sincon.troubleticketing.enums.Priority;
import com.sincon.troubleticketing.enums.Status;
import com.sincon.troubleticketing.ticketHistory.TicketHistory;
import com.sincon.troubleticketing.user.User;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;
    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @JsonManagedReference
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    @JsonManagedReference
    private User assignedTo;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<TicketHistory> histories = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<Attachment> attachments = new HashSet<>();

    // Helper methods for bidirectional relationships

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setTicket(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setTicket(null);
    }

    public void addHistory(TicketHistory history) {
        histories.add(history);
        history.setTicket(this);
    }

    public void removeHistory(TicketHistory history) {
        histories.remove(history);
        history.setTicket(null);
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setTicket(this);
    }

    public void removeAttachment(Attachment attachment) {
        attachments.remove(attachment);
        attachment.setTicket(null);
    }

    // Business methods

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }

    public boolean isResolved() {
        return status == Status.RESOLVED;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    public void markInProgress(User agent) {
        if (this.status == Status.OPEN) {
            this.status = Status.IN_PROGRESS;
            this.assignedTo = agent;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void resolve() {
        if (this.status == Status.IN_PROGRESS) {
            this.status = Status.RESOLVED;
            this.resolvedAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void close() {
        if (this.status == Status.RESOLVED) {
            this.status = Status.CLOSED;
            this.closedAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void reopen() {
        if (this.status == Status.RESOLVED || this.status == Status.CLOSED) {
            this.status = Status.OPEN;
            this.resolvedAt = null;
            this.closedAt = null;
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}