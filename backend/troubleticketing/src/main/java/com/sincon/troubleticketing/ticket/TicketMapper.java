package com.sincon.troubleticketing.ticket;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.sincon.troubleticketing.category.Category;
import com.sincon.troubleticketing.category.CategoryMapper;
import com.sincon.troubleticketing.category.CategoryRepository;
import com.sincon.troubleticketing.comment.CommentMapper;
import com.sincon.troubleticketing.ticketHistory.TicketHistoryMapper;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserMapper;
import com.sincon.troubleticketing.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, CommentMapper.class, TicketHistoryMapper.class})
@RequiredArgsConstructor
public abstract class TicketMapper {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "assignedToId", source = "assignedTo.id")
    @Mapping(target = "categoryId", source = "category.id")
    public abstract TicketDTO toDTO(Ticket ticket);
    
    @Mapping(target = "createdBy", source = "createdById", qualifiedByName = "userIdToUser")
    @Mapping(target = "assignedTo", source = "assignedToId", qualifiedByName = "userIdToUser")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "history", ignore = true)
    public abstract Ticket toEntity(TicketDTO ticketDTO);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "assignedTo", source = "assignedToId", qualifiedByName = "userIdToUser")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    public abstract void updateTicketFromDTO(TicketDTO ticketDTO, @MappingTarget Ticket ticket);
    
    @Named("userIdToUser")
    protected User userIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.orElse(null);
    }
    
    @Named("categoryIdToCategory")
    protected Category categoryIdToCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        return categoryOpt.orElse(null);
    }
}
