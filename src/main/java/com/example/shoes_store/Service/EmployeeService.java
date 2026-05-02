package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.Employee;
import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Repo.EmployeeRepo;
import com.example.shoes_store.Repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepo employeeRepository;
    @Autowired
    private UserRepo userRepository;

    // Lấy tất cả nhân viên
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    // Lấy nhân viên theo ID
    public Optional<Employee> getById(Long id) {
        return employeeRepository.findById(id);
    }

//    // Lấy nhân viên đang làm việc
//    public List<Employee> getAllActive() {
//        return employeeRepository.findByStatus();
//    }

    // Lấy nhân viên theo chức vụ
    public List<Employee> getByPosition(String position) {
        return employeeRepository.findByPosition(position);
    }

    // Lấy nhân viên theo User ID
    public Optional<Employee> getByUserId(Long userId) {
        return employeeRepository.findByUserId(userId);
    }

    // Thêm nhân viên mới
    public Employee addEmployee(Employee employee) {
        // Kiểm tra mã nhân viên đã tồn tại chưa
        if (employeeRepository.existsByCode(employee.getCode())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại: " + employee.getCode());
        }
        User u= userRepository.findById(employee.getUserId()).get();
        if(!u.getRole().equals("USER")){
            throw new RuntimeException("Tài khoản: " + u.getUsername()  + " với mã tài khoản "+ u.getId() + " không phải nhân viên cửa hàng.");
        }
//        employee.setCreatedAt(LocalDateTime.now());
//        employee.setUpdatedAt(LocalDateTime.now());
        if (employee.getStatus() == null) {
            employee.setStatus("ACTIVE");
        }
        return employeeRepository.save(employee);
    }

    // Cập nhật nhân viên
    public Employee updateEmployee(Employee employee) {
        Optional<Employee> existing = employeeRepository.findById(employee.getId());
        if (existing.isPresent()) {
            Employee updated = existing.get();

            // Kiểm tra mã nhân viên trùng (trừ chính nó)
            if (!updated.getCode().equals(employee.getCode()) &&
                    employeeRepository.existsByCode(employee.getCode())) {
                throw new RuntimeException("Mã nhân viên đã tồn tại: " + employee.getCode());
            }
            User u= userRepository.findById(employee.getUserId()).get();
            if(!u.getRole().equals("USER")){
                throw new RuntimeException("Tài khoản: " + u.getUsername()  + " với mã tài khoản "+ u.getId() + " không phải nhân viên cửa hàng.");
            }
            updated.setCode(employee.getCode());
            updated.setFullName(employee.getFullName());
            updated.setPhone(employee.getPhone());
            updated.setEmail(employee.getEmail());
            updated.setPosition(employee.getPosition());
            updated.setHireDate(employee.getHireDate());
            updated.setStatus(employee.getStatus());
            updated.setUserId(employee.getUserId());

            return employeeRepository.save(updated);
        }
        throw new RuntimeException("Không tìm thấy nhân viên với ID: " + employee.getId());
    }

    // Xóa nhân viên
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    // Cập nhật trạng thái nhân viên
    public Employee updateEmployeeStatus(Long id, String status) {
        Optional<Employee> existing = employeeRepository.findById(id);
        if (existing.isPresent()) {
            Employee employee = existing.get();
            employee.setStatus(status);
            return employeeRepository.save(employee);
        }
        throw new RuntimeException("Không tìm thấy nhân viên với ID: " + id);
    }

//    // Tìm kiếm theo tên
//    public List<Employee> searchByName(String name) {
//        return employeeRepository.findByFullNameContainingIgnoreCase(name);
//    }

    // Tìm kiếm theo mã
    public Optional<Employee> searchByCode(String code) {
        return employeeRepository.findByCode(code);
    }
}