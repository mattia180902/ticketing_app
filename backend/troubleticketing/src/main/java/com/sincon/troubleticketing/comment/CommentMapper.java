package com.sincon.troubleticketing.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.sincon.troubleticketing.ticket.Ticket;
import com.sincon.troubleticketing.ticket.TicketRepository;
import com.sincon.troubleticketing.user.User;
import com.sincon.troubleticketing.user.UserMapper;
import com.sincon.troubleticketing.user.UserRepository;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class CommentMapper {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "userId", source = "user.id")
    public abstract CommentDTO toDTO(Comment comment);
    
    @Mapping(target = "ticket", source = "ticketId", qualifiedByName = "ticketIdToTicket")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    public abstract Comment toEntity(CommentDTO commentDTO);
    
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