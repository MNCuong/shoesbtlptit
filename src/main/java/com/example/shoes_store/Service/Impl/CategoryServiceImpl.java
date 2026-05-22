package com.example.shoes_store.Service.Impl;

import com.example.shoes_store.Entity.Category;
import com.example.shoes_store.Entity.Product;
import com.example.shoes_store.Repo.CategoryRepo;
import com.example.shoes_store.Repo.ProductRepo;
import com.example.shoes_store.Service.CategoryService;
import com.example.shoes_store.Service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductService productService;
    @Override
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    @Override
    public List<Category> getAllWithActive() {
        return categoryRepo.findByActive(true);
    }

    @Override
    public Category addCategory(Category category) {
        category.setActive(true);
        return categoryRepo.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        if (categoryRepo.existsById(category.getId())) {
            category.setActive(true);
            return categoryRepo.save(category);
        } else {
            throw new EntityNotFoundException("Category with ID " + category.getId() + " not found");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found");
        }

        // 1. Lấy tất cả products thuộc category này
        List<Product> products = productRepo.findByCategory_Id(id);

        // 2. Xóa từng product (method deleteProduct đã có xử lý xóa bảng phụ)
        for (Product product : products) {
            productService.deleteProduct(product.getId());  // Gọi lại method đã có
        }

        // 3. Xóa category
        categoryRepo.deleteById(id);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
    }

    @Override
    public Category toggleCategoryStatus(Long id) {
        Category category = categoryRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));

        category.setActive(!category.isActive());
        return categoryRepo.save(category);
    }


}
