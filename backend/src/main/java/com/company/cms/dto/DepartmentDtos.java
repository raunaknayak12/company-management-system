package com.company.cms.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class DepartmentDtos {

    public static class DepartmentRequest {
        @NotBlank(message = "Department name is required")
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class DepartmentResponse {
        private Long id;
        private String name;
        private String description;
        private long employeeCount;
        private LocalDateTime createdAt;

        public DepartmentResponse(Long id, String name, String description, long employeeCount, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.employeeCount = employeeCount;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public long getEmployeeCount() { return employeeCount; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
