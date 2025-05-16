package com.sincon.troubleticketing.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    @Query("SELECT c FROM Category c ORDER BY c.name")
    List<Category> findAllOrderByName();
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.category.id = :categoryId")
    Long countTicketsByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT c.id, c.name, COUNT(t) FROM Category c LEFT JOIN c.tickets t GROUP BY c.id, c.name ORDER BY COUNT(t) DESC")
    List<Object[]> findAllWithTicketCount();
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.tickets t WHERE c.id = :categoryId")
    Optional<Category> findByIdWithTickets(@Param("categoryId") Long categoryId);
    
    @Query(value = "SELECT c.*, COUNT(t.id) as ticket_count FROM categories c " +
           "LEFT JOIN tickets t ON c.id = t.category_id " +
           "GROUP BY c.id ORDER BY ticket_count DESC", 
           nativeQuery = true)
    List<Object[]> findAllCategoriesWithTicketCountNative();
}