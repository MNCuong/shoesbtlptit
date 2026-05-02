package com.example.shoes_store.Controller;


import com.example.shoes_store.Entity.Supplier;
import com.example.shoes_store.Service.SupperlieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/suppliers")
public class SupperlieController {

    @Autowired
    private SupperlieService supplierService;

    @PostMapping("/add")
    public String addSupplier(@RequestBody Supplier supplier) {
        supplierService.addSupplier(supplier);
        return "redirect:/admin/home";
    }

    @PostMapping("/edit/{id}")
    public String editSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierService.updateSupplier(supplier);
        return "redirect:/admin/home";
    }

    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "redirect:/admin/home";
    }
}
