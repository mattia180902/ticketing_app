package com.sincon.troubleticketing.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDTO {
    
    private Long id;
    
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private boolean internal;
    
    // Relationships
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    // Denormalized fields for display
    private String userName;
    private String userRole;
    private String userAvatarUrl;
}
