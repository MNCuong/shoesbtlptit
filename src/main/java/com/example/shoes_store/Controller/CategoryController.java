package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.Category;
import com.example.shoes_store.Service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // API trả về JSON cho frontend fetch
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<List<Category>> getAllCategoriesJson() {
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(categories);
    }

    // API thêm category (trả về JSON)
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addCategory(@RequestBody Category category) {
        try {
            Category saved = categoryService.addCategory(category);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // API sửa category (trả về JSON)
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            category.setId(id);
            Category updated = categoryService.updateCategory(category);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // API xóa category (trả về JSON)
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== GIỮ LẠI CÁC METHOD CHO THYMLEAF (nếu cần) ==========

    @GetMapping
    public String getAllCategories(Model model) {
        List<Category> categories = categoryService.getAll();
        log.info("categories: {}", categories.get(0).getName());
        model.addAttribute("categories", categories);
        return "redirect:/admin/home";
    }

    @GetMapping("/active")
    public String getAllActiveCategories(Model model) {
        List<Category> activeCategories = categoryService.getAllWithActive();
        model.addAttribute("categories", activeCategories);
        return "/category/list";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleCategoryStatus(@PathVariable Long id) {
        categoryService.toggleCategoryStatus(id);
        return "redirect:/admin/home";
    }
}