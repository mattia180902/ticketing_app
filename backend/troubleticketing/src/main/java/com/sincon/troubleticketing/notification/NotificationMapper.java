package com.sincon.troubleticketing.notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.ticket.TicketMapper;
import com.sincon.troubleticketing.ticket.TicketRepository;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserMapper;
import com.sincon.troubleticketing.user.UserRepository;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = {UserMapper.class, TicketMapper.class})
public abstract class NotificationMapper {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ticketId", source = "ticket.id")
    public abstract NotificationDTO toDTO(Notification notification);
    
    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    @Mapping(target = "ticket", source = "ticketId", qualifiedByName = "ticketIdToTicket")
    public abstract Notification toEntity(NotificationDTO notificationDTO);
    
    @Named("userIdToUser")
    protected User userIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.orElse(null);
    }
    
    @Named("ticketIdToTicket")
    protected Ticket ticketIdToTicket(Long ticketId) {
        if (ticketId == null) {
            return null;
        }
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        return ticketOpt.orElse(null);
    }
}