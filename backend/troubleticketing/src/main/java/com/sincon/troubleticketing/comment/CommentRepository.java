package com.sincon.troubleticketing.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByTicketId(Long ticketId);
    
    Page<Comment> findByTicketId(Long ticketId, Pageable pageable);
    
    List<Comment> findByUserId(Long userId);
    
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId ORDER BY c.createdAt DESC")
    List<Comment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.ticket.id = :ticketId")
    Long countByTicketId(Long ticketId);
    
    @Query("SELECT c FROM Comment c JOIN c.ticket t WHERE t.id = :ticketId AND c.createdAt = " +
           "(SELECT MIN(c2.createdAt) FROM Comment c2 WHERE c2.ticket.id = :ticketId)")
    Comment findFirstResponseByTicketId(Long ticketId);

    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId ORDER BY c.createdAt ASC")
    Collection<CommentDTO> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
