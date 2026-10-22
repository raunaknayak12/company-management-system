package com.company.cms.controller;

import com.company.cms.dto.EmployeeDtos.*;
import com.company.cms.entity.Role;
import com.company.cms.security.UserPrincipal;
import com.company.cms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Endpoints for employee personnel management and profile access")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Search and filter employees (Salary visible to Admin only)")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Role role = userPrincipal.getRole();
        return ResponseEntity.ok(employeeService.searchEmployees(departmentId, status, search, role));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user's employee profile")
    public ResponseEntity<EmployeeResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(employeeService.getEmployeeByUserId(userPrincipal.getId(), userPrincipal.getRole()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get employee details by ID")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id, userPrincipal.getRole()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Create a new employee record")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request, userPrincipal.getRole()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update employee record")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request, userPrincipal.getRole()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete or deactivate employee")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee processed successfully"));
    }
}
