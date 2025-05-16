package com.sincon.troubleticketing.ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sincon.troubleticketing.enums.*;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    
    // Basic queries
    List<Ticket> findByStatus(Status status);
    
    List<Ticket> findByPriority(Priority priority);
    
    List<Ticket> findByCategoryId(Long categoryId);
    
    List<Ticket> findByCreatedById(Long userId);
    
    List<Ticket> findByAssignedToId(Long userId);
    
    // Advanced queries with multiple conditions
    List<Ticket> findByStatusAndPriority(Status status, Priority priority);
    
    List<Ticket> findByStatusInAndPriorityIn(List<Status> statuses, List<Priority> priorities);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:createdById IS NULL OR t.createdBy.id = :createdById) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    List<Ticket> findTicketsWithFilters(
            @Param("status") Status status,
            @Param("priority") Priority priority,
            @Param("categoryId") Long categoryId,
            @Param("createdById") Long createdById,
            @Param("assignedToId") Long assignedToId);
    
    // Paginated queries
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:createdById IS NULL OR t.createdBy.id = :createdById) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<Ticket> findTicketsWithFiltersPaginated(
            @Param("status") Status status,
            @Param("priority") Priority priority,
            @Param("categoryId") Long categoryId,
            @Param("createdById") Long createdById,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable);
    
    // Search query
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ticket> searchTickets(@Param("searchTerm") String searchTerm);
    
    // Fetch with related entities
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN FETCH t.category " +
           "LEFT JOIN FETCH t.createdBy " +
           "LEFT JOIN FETCH t.assignedTo " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN FETCH t.category " +
           "LEFT JOIN FETCH t.createdBy " +
           "LEFT JOIN FETCH t.assignedTo " +
           "WHERE t.status = :status")
    List<Ticket> findByStatusWithDetails(@Param("status") Status status);
    
    // Statistics queries
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority = :priority")
    Long countByPriority(@Param("priority") Priority priority);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.category.id = :categoryId")
    Long countByCategory(@Param("categoryId") Long categoryId);
    
    @Query("SELECT AVG(DATEDIFF(t.resolvedAt, t.createdAt)) FROM Ticket t WHERE t.status = 'RESOLVED'")
    Double getAverageResolutionTimeInDays();
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Long countTicketsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) as month, " +
           "EXTRACT(YEAR FROM created_at) as year, COUNT(*) as ticket_count " +
           "FROM tickets " +
           "WHERE created_at >= :startDate AND created_at <= :endDate " +
           "GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at) " +
           "ORDER BY year, month",
           nativeQuery = true)
    List<Object[]> getTicketCountByMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    long countByStatusAndCreationDateBetween(String string, LocalDate startDate, LocalDate endDate);

    Map<String, Long> countTicketsByCategory();

    Map<String, Long> countTicketsByStatus();

    Map<String, Long> countTicketsByPriority();

    Map<String, Long> countTicketsByAgent();

    Map<String, Long> countTicketsByMonth(Integer months);

    Map<String, Double> calculateAverageResponseTimes();

    Map<String, Double> calculateAverageResolutionTimes();
}