package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.Employee;
import com.example.shoes_store.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/add")
    public String addEmployee(@RequestBody Employee employee) {
        employeeService.addEmployee(employee);
        return "redirect:/admin/home";
    }

    @PostMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        employeeService.updateEmployee(employee);
        return "redirect:/admin/home";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/admin/home";
    }
}