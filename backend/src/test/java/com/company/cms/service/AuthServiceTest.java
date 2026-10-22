package com.company.cms.service;

import com.company.cms.dto.AuthDtos.*;
import com.company.cms.entity.Role;
import com.company.cms.entity.User;
import com.company.cms.exception.ConflictException;
import com.company.cms.repository.EmployeeRepository;
import com.company.cms.repository.UserRepository;
import com.company.cms.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should successfully authenticate user and return JWT login response")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@company.com");
        req.setPassword("Password@123");

        User user = new User("test@company.com", "encoded-pass", Role.ADMIN);
        user.setId(1L);

        when(userRepository.findByEmail("test@company.com")).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(jwtService.generateToken("test@company.com", Role.ADMIN, 1L)).thenReturn("jwt-token-123");

        LoginResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("jwt-token-123", resp.getToken());
        assertEquals("test@company.com", resp.getEmail());
        assertEquals(Role.ADMIN, resp.getRole());
    }

    @Test
    @DisplayName("Should reject user creation when email is already registered")
    void testCreateUserConflict() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("existing@company.com");
        req.setPassword("Secret123");
        req.setRole(Role.EMPLOYEE);

        when(userRepository.existsByEmail("existing@company.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.createUser(req));
        verify(userRepository, never()).save(any());
    }
}
