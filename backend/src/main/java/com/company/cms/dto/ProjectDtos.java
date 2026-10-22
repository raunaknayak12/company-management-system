package com.company.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class ProjectDtos {

    public static class ProjectRequest {
        @NotBlank(message = "Project name is required")
        private String name;
        private String description;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;
        private LocalDate endDate;
        private String status = "PLANNED";

        @NotNull(message = "Department ID is required")
        private Long departmentId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    }

    public static class AssignEmployeeRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    }

    public static class ProjectResponse {
        private Long id;
        private String name;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private Long departmentId;
        private String departmentName;
        private List<EmployeeDtos.EmployeeResponse> assignedEmployees;

        public ProjectResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public List<EmployeeDtos.EmployeeResponse> getAssignedEmployees() { return assignedEmployees; }
        public void setAssignedEmployees(List<EmployeeDtos.EmployeeResponse> assignedEmployees) { this.assignedEmployees = assignedEmployees; }
    }
}
