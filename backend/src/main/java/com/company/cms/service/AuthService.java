package com.company.cms.service;

import com.company.cms.dto.AuthDtos.*;
import com.company.cms.entity.Employee;
import com.company.cms.entity.Role;
import com.company.cms.entity.User;
import com.company.cms.exception.BadRequestException;
import com.company.cms.exception.ConflictException;
import com.company.cms.exception.ResourceNotFoundException;
import com.company.cms.repository.EmployeeRepository;
import com.company.cms.repository.UserRepository;
import com.company.cms.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password."));

        if (!user.isActive()) {
            throw new BadRequestException("User account is inactive. Please contact your system administrator.");
        }

        Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        String fullName = employee != null ? employee.getFullName() : user.getEmail();
        Long employeeId = employee != null ? employee.getId() : null;

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());

        return new LoginResponse(token, user.getEmail(), user.getRole(), fullName, employeeId);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists.");
        }

        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getRole());
        user = userRepository.save(user);

        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive());
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.isActive()))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Security check: Never deactivate the last active Admin
        if (user.isActive() && user.getRole() == Role.ADMIN) {
            long activeAdminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.isActive())
                    .count();
            if (activeAdminCount <= 1) {
                throw new BadRequestException("Cannot deactivate the last remaining active Administrator account.");
            }
        }

        user.setActive(!user.isActive());
        user = userRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive());
    }
}
