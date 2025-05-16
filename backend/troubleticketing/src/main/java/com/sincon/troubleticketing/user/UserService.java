package com.sincon.troubleticketing.user;

import com.sincon.troubleticketing.enums.*;
import com.sincon.troubleticketing.exception.ResourceNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        UserDTO dto = convertToDTO(user);
        enrichWithStatistics(dto, user);
        return dto;
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        UserDTO dto = convertToDTO(user);
        enrichWithStatistics(dto, user);
        return dto;
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Check if username or email already exists
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = convertToEntity(userDTO);
        
        // Set a default role if not provided
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        
        // Encrypt password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
                userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
                userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Update fields
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setRole(userDTO.getRole());
        existingUser.setDepartment(userDTO.getDepartment());
        existingUser.setAvatarUrl(userDTO.getAvatarUrl());
        existingUser.setActive(userDTO.isActive());
        
        // Update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setActive(false);
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .build();
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setDepartment(dto.getDepartment());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setActive(dto.isActive());
        return user;
    }

    private void enrichWithStatistics(UserDTO dto, User user) {
        dto.setAssignedTicketsCount(userRepository.countAssignedTickets(user.getId()).intValue());
        dto.setCreatedTicketsCount(userRepository.countCreatedTickets(user.getId()).intValue());
    }
}
