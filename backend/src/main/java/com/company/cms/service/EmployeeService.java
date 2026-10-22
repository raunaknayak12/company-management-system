package com.company.cms.service;

import com.company.cms.dto.EmployeeDtos.*;
import com.company.cms.entity.Department;
import com.company.cms.entity.Employee;
import com.company.cms.entity.Role;
import com.company.cms.exception.ConflictException;
import com.company.cms.exception.ResourceNotFoundException;
import com.company.cms.repository.DepartmentRepository;
import com.company.cms.repository.EmployeeRepository;
import com.company.cms.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalesOrderRepository salesOrderRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            SalesOrderRepository salesOrderRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployees(Long deptId, String status, String search, Role userRole) {
        boolean hasDept = deptId != null;
        boolean hasStatus = status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL");
        boolean hasSearch = search != null && !search.isBlank();

        List<Employee> list;
        if (!hasDept && !hasStatus && !hasSearch) {
            list = employeeRepository.findAll();
        } else {
            String sVal = hasStatus ? status.trim() : null;
            String qVal = hasSearch ? search.trim().toLowerCase() : null;
            list = employeeRepository.searchEmployees(deptId, sVal, qVal);
        }
        return list.stream()
                .map(e -> mapToResponse(e, userRole))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id, Role userRole) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponse(employee, userRole);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId, Role userRole) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile linked to user id: " + userId));
        return mapToResponse(employee, userRole);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email, Role userRole) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile linked to email: " + email));
        return mapToResponse(employee, userRole);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request, Role userRole) {
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new ConflictException("Employee code " + request.getEmployeeCode() + " is already taken.");
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email " + request.getEmail() + " is already registered.");
        }

        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        Employee emp = new Employee();
        emp.setEmployeeCode(request.getEmployeeCode());
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setJobTitle(request.getJobTitle());
        emp.setHireDate(request.getHireDate());
        emp.setSalary(request.getSalary());
        emp.setDateOfBirth(request.getDateOfBirth());
        emp.setDepartment(dept);
        emp.setManager(manager);

        emp = employeeRepository.save(emp);
        return mapToResponse(emp, userRole);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request, Role userRole) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!emp.getEmployeeCode().equalsIgnoreCase(request.getEmployeeCode()) &&
                employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new ConflictException("Employee code " + request.getEmployeeCode() + " is already taken.");
        }
        if (!emp.getEmail().equalsIgnoreCase(request.getEmail()) &&
                employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email " + request.getEmail() + " is already registered.");
        }

        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Employee manager = null;
        if (request.getManagerId() != null && !request.getManagerId().equals(id)) {
            manager = employeeRepository.findById(request.getManagerId()).orElse(null);
        }

        emp.setEmployeeCode(request.getEmployeeCode());
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setJobTitle(request.getJobTitle());
        emp.setHireDate(request.getHireDate());
        emp.setSalary(request.getSalary());
        emp.setDateOfBirth(request.getDateOfBirth());
        emp.setDepartment(dept);
        emp.setManager(manager);

        emp = employeeRepository.save(emp);
        return mapToResponse(emp, userRole);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // FR-EMP-5: If has sales history, deactivate and anonymize personal PII rather than hard delete
        boolean hasOrders = !salesOrderRepository.findBySalesRepId(id).isEmpty();
        if (hasOrders) {
            emp.setStatus("INACTIVE");
            emp.setFirstName("Former");
            emp.setLastName("Employee");
            emp.setEmail("former_emp_" + emp.getId() + "@anonymized.internal");
            emp.setPhone(null);
            emp.setSalary(null);
            emp.setDateOfBirth(null);
            employeeRepository.save(emp);
        } else {
            employeeRepository.delete(emp);
        }
    }

    private EmployeeResponse mapToResponse(Employee emp, Role userRole) {
        EmployeeResponse resp = new EmployeeResponse();
        resp.setId(emp.getId());
        resp.setEmployeeCode(emp.getEmployeeCode());
        resp.setFirstName(emp.getFirstName());
        resp.setLastName(emp.getLastName());
        resp.setFullName(emp.getFullName());
        resp.setEmail(emp.getEmail());
        resp.setPhone(emp.getPhone());
        resp.setJobTitle(emp.getJobTitle());
        resp.setHireDate(emp.getHireDate());
        resp.setStatus(emp.getStatus());

        if (emp.getDepartment() != null) {
            resp.setDepartmentId(emp.getDepartment().getId());
            resp.setDepartmentName(emp.getDepartment().getName());
        }

        if (emp.getManager() != null) {
            resp.setManagerId(emp.getManager().getId());
            resp.setManagerName(emp.getManager().getFullName());
        }

        // FR-EMP-4: Salary & DOB only visible to Admin
        if (userRole == Role.ADMIN) {
            resp.setSalary(emp.getSalary());
            resp.setDateOfBirth(emp.getDateOfBirth());
        }

        return resp;
    }
}
