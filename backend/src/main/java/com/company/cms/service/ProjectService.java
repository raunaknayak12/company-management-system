package com.company.cms.service;

import com.company.cms.dto.EmployeeDtos.EmployeeResponse;
import com.company.cms.dto.ProjectDtos.*;
import com.company.cms.entity.Department;
import com.company.cms.entity.Employee;
import com.company.cms.entity.Project;
import com.company.cms.entity.Role;
import com.company.cms.exception.ResourceNotFoundException;
import com.company.cms.repository.DepartmentRepository;
import com.company.cms.repository.EmployeeRepository;
import com.company.cms.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository
    ) {
        this.projectRepository = projectRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsForUser(Role role, Long employeeId) {
        if (role == Role.EMPLOYEE && employeeId != null) {
            // FR-PRJ-3: Employee sees only assigned projects
            return projectRepository.findProjectsAssignedToEmployee(employeeId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Project p = new Project();
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());
        p.setStatus(request.getStatus() != null ? request.getStatus() : "PLANNED");
        p.setDepartment(dept);

        p = projectRepository.save(p);
        return mapToResponse(p);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());
        p.setStatus(request.getStatus());
        p.setDepartment(dept);

        p = projectRepository.save(p);
        return mapToResponse(p);
    }

    @Transactional
    public ProjectResponse assignEmployee(Long projectId, Long employeeId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        p.getAssignedEmployees().add(emp);
        p = projectRepository.save(p);
        return mapToResponse(p);
    }

    @Transactional
    public ProjectResponse removeEmployee(Long projectId, Long employeeId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        p.getAssignedEmployees().remove(emp);
        p = projectRepository.save(p);
        return mapToResponse(p);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        if (p.getAssignedEmployees() != null) {
            p.getAssignedEmployees().clear();
            projectRepository.save(p);
        }
        projectRepository.delete(p);
    }

    private ProjectResponse mapToResponse(Project p) {
        ProjectResponse resp = new ProjectResponse();
        resp.setId(p.getId());
        resp.setName(p.getName());
        resp.setDescription(p.getDescription());
        resp.setStartDate(p.getStartDate());
        resp.setEndDate(p.getEndDate());
        resp.setStatus(p.getStatus());
        if (p.getDepartment() != null) {
            resp.setDepartmentId(p.getDepartment().getId());
            resp.setDepartmentName(p.getDepartment().getName());
        }
        if (p.getAssignedEmployees() != null) {
            resp.setAssignedEmployees(p.getAssignedEmployees().stream().map(e -> {
                EmployeeResponse er = new EmployeeResponse();
                er.setId(e.getId());
                er.setEmployeeCode(e.getEmployeeCode());
                er.setFullName(e.getFullName());
                er.setEmail(e.getEmail());
                er.setJobTitle(e.getJobTitle());
                return er;
            }).collect(Collectors.toList()));
        }
        return resp;
    }
}
