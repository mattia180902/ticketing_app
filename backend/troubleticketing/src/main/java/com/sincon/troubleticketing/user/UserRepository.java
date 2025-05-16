package com.sincon.troubleticketing.user;

import com.sincon.troubleticketing.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(Role role);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :userId")
    Long countAssignedTickets(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdBy.id = :userId")
    Long countCreatedTickets(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :userId AND t.status = 'RESOLVED'")
    Long countResolvedTickets(@Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE u.role = 'AGENT' ORDER BY u.lastName, u.firstName")
    List<User> findAllAgents();
    
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.lastName, u.firstName")
    List<User> findAllActiveUsers();
    
    @Query("SELECT u, COUNT(t) as ticketCount FROM User u LEFT JOIN u.assignedTickets t " +
           "WHERE u.role = 'AGENT' GROUP BY u ORDER BY ticketCount DESC")
    List<Object[]> findAgentsWithTicketCounts();
    
    @Query(value = "SELECT u.*, " +
           "(SELECT COUNT(t.id) FROM tickets t WHERE t.assigned_to_id = u.id) AS assigned_count, " +
           "(SELECT COUNT(t.id) FROM tickets t WHERE t.created_by_id = u.id) AS created_count " +
           "FROM users u WHERE u.active = true ORDER BY u.last_name, u.first_name",
           nativeQuery = true)
    List<Object[]> findAllActiveUsersWithTicketCounts();
}
