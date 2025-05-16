package com.sincon.troubleticketing.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sincon.troubleticketing.category.Category;
import com.sincon.troubleticketing.category.CategoryRepository;
import com.sincon.troubleticketing.enums.*;
import com.sincon.troubleticketing.exception.ResourceNotFoundException;
import com.sincon.troubleticketing.ticketHistory.TicketHistoryService;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TicketHistoryService historyService;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TicketHistoryService historyService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.historyService = historyService;
    }

    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketDTO> getTicketsWithFilters(
            Status status, 
            Priority priority, 
            Long categoryId, 
            Long createdById, 
            Long assignedToId) {
        return ticketRepository.findTicketsWithFilters(status, priority, categoryId, createdById, assignedToId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<TicketDTO> getTicketsWithFiltersPaginated(
            Status status, 
            Priority priority, 
            Long categoryId, 
            Long createdById, 
            Long assignedToId,
            Pageable pageable) {
        return ticketRepository.findTicketsWithFiltersPaginated(
                status, priority, categoryId, createdById, assignedToId, pageable)
                .map(this::convertToDTO);
    }
    
    public List<TicketDTO> searchTickets(String searchTerm) {
        return ticketRepository.searchTickets(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        
        return convertToDTOWithDetails(ticket);
    }

    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO, Long userId) {
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Ticket ticket = new Ticket();
        ticket.setSubject(ticketDTO.getSubject());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setStatus(Status.OPEN);
        ticket.setPriority(ticketDTO.getPriority());
        ticket.setCreatedBy(createdBy);
        ticket.setCreatedAt(LocalDateTime.now());
        
        // Set category if provided
        if (ticketDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(ticketDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + ticketDTO.getCategoryId()));
            ticket.setCategory(category);
        }
        
        // Set assignee if provided
        if (ticketDTO.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(ticketDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ticketDTO.getAssignedToId()));
            ticket.setAssignedTo(assignedTo);
            ticket.setStatus(Status.IN_PROGRESS);
        }
        
        // Set due date if provided
        if (ticketDTO.getDueDate() != null) {
            ticket.setDueDate(ticketDTO.getDueDate());
        }
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create ticket history entry
        historyService.createTicketHistory(savedTicket, "status", null, savedTicket.getStatus().name(), userId);
        
        return convertToDTO(savedTicket);
    }

    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO, Long userId) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        
        User updatedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Track changes for history
        trackChanges(existingTicket, ticketDTO, userId);
        
        // Update fields
        existingTicket.setSubject(ticketDTO.getSubject());
        existingTicket.setDescription(ticketDTO.getDescription());
        existingTicket.setPriority(ticketDTO.getPriority());
        existingTicket.setUpdatedAt(LocalDateTime.now());
        
        // Update category if changed
        if (ticketDTO.getCategoryId() != null && 
                (existingTicket.getCategory() == null || !existingTicket.getCategory().getId().equals(ticketDTO.getCategoryId()))) {
            Category category = categoryRepository.findById(ticketDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + ticketDTO.getCategoryId()));
            existingTicket.setCategory(category);
        }
        
        // Update assigned user if changed
        if (ticketDTO.getAssignedToId() != null && 
                (existingTicket.getAssignedTo() == null || !existingTicket.getAssignedTo().getId().equals(ticketDTO.getAssignedToId()))) {
            User assignedTo = userRepository.findById(ticketDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ticketDTO.getAssignedToId()));
            existingTicket.setAssignedTo(assignedTo);
            
            // If ticket is open and being assigned, automatically set to in progress
            if (existingTicket.getStatus() == Status.OPEN) {
                existingTicket.setStatus(Status.IN_PROGRESS);
                historyService.createTicketHistory(existingTicket, "status", Status.OPEN.name(), Status.IN_PROGRESS.name(), userId);
            }
        }
        
        // Update status if changed
        if (ticketDTO.getStatus() != null && existingTicket.getStatus() != ticketDTO.getStatus()) {
            Status oldStatus = existingTicket.getStatus();
            existingTicket.setStatus(ticketDTO.getStatus());
            
            // Update timestamp based on status change
            if (ticketDTO.getStatus() == Status.RESOLVED) {
                existingTicket.setResolvedAt(LocalDateTime.now());
            } else if (ticketDTO.getStatus() == Status.CLOSED) {
                existingTicket.setClosedAt(LocalDateTime.now());
            }
            
            historyService.createTicketHistory(existingTicket, "status", oldStatus.name(), ticketDTO.getStatus().name(), userId);
        }
        
        // Update due date if provided
        if (ticketDTO.getDueDate() != null) {
            existingTicket.setDueDate(ticketDTO.getDueDate());
        }
        
        Ticket updatedTicket = ticketRepository.save(existingTicket);
        return convertToDTO(updatedTicket);
    }

    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket not found with id: " + id);
        }
        ticketRepository.deleteById(id);
    }
    
    @Transactional
    public TicketDTO changeStatus(Long id, Status newStatus, Long userId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        
        Status oldStatus = ticket.getStatus();
        
        if (oldStatus == newStatus) {
            return convertToDTO(ticket);
        }
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if (newStatus == Status.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (newStatus == Status.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        } else if ((newStatus == Status.OPEN || newStatus == Status.IN_PROGRESS) && 
                  (oldStatus == Status.RESOLVED || oldStatus == Status.CLOSED)) {
            // If reopening a resolved or closed ticket
            ticket.setResolvedAt(null);
            ticket.setClosedAt(null);
        }
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Create history entry
        historyService.createTicketHistory(updatedTicket, "status", oldStatus.name(), newStatus.name(), userId);
        
        return convertToDTO(updatedTicket);
    }
    
    @Transactional
    public TicketDTO assignTicket(Long id, Long assigneeId, Long userId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assigneeId));
        
        String oldAssignee = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : "None";
        
        ticket.setAssignedTo(assignee);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // If ticket is open, change to in progress
        if (ticket.getStatus() == Status.OPEN) {
            Status oldStatus = ticket.getStatus();
            ticket.setStatus(Status.IN_PROGRESS);
            historyService.createTicketHistory(ticket, "status", oldStatus.name(), Status.IN_PROGRESS.name(), userId);
        }
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Create history entry
        historyService.createTicketHistory(updatedTicket, "assignee", oldAssignee, assignee.getUsername(), userId);
        
        return convertToDTO(updatedTicket);
    }
    
    private void trackChanges(Ticket existingTicket, TicketDTO newTicketDTO, Long userId) {
        // Track subject change
        if (!existingTicket.getSubject().equals(newTicketDTO.getSubject())) {
            historyService.createTicketHistory(
                    existingTicket, 
                    "subject", 
                    existingTicket.getSubject(), 
                    newTicketDTO.getSubject(), 
                    userId);
        }
        
        // Track priority change
        if (existingTicket.getPriority() != newTicketDTO.getPriority()) {
            historyService.createTicketHistory(
                    existingTicket, 
                    "priority", 
                    existingTicket.getPriority().name(), 
                    newTicketDTO.getPriority().name(), 
                    userId);
        }
        
        // Track category change
        Long existingCategoryId = existingTicket.getCategory() != null ? existingTicket.getCategory().getId() : null;
        if ((existingCategoryId == null && newTicketDTO.getCategoryId() != null) || 
            (existingCategoryId != null && !existingCategoryId.equals(newTicketDTO.getCategoryId()))) {
            
            String oldCategory = existingTicket.getCategory() != null ? existingTicket.getCategory().getName() : "None";
            String newCategory = "Unknown";
            
            if (newTicketDTO.getCategoryId() != null) {
                categoryRepository.findById(newTicketDTO.getCategoryId()).ifPresent(category -> {
                    newCategory = category.getName();
                });
            }
            
            historyService.createTicketHistory(
                    existingTicket, 
                    "category", 
                    oldCategory, 
                    newCategory, 
                    userId);
        }
    }
    
    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setClosedAt(ticket.getClosedAt());
        dto.setDueDate(ticket.getDueDate());
        
        // Set relations
        if (ticket.getCategory() != null) {
            dto.setCategoryId(ticket.getCategory().getId());
            dto.setCategoryName(ticket.getCategory().getName());
        }
        
        if (ticket.getCreatedBy() != null) {
            dto.setCreatedById(ticket.getCreatedBy().getId());
            dto.setCreatedByName(ticket.getCreatedBy().getFirstName() + " " + ticket.getCreatedBy().getLastName());
        }
        
        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getId());
            dto.setAssignedToName(ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName());
        }
        
        return dto;
    }
    
    private TicketDTO convertToDTOWithDetails(Ticket ticket) {
        TicketDTO dto = convertToDTO(ticket);
        
        // Additional fields like comments, history, etc. would be loaded here
        // This would typically involve loading from CommentService, HistoryService, etc.
        
        return dto;
    }
}