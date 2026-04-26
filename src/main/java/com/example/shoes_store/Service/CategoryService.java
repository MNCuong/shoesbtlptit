package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.Category;
import com.example.shoes_store.Repo.CategoryRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CategoryService {

    List<Category> getAll();

    List<Category> getAllWithActive();

    Category addCategory(Category category);

    Category updateCategory(Category category);

    void deleteCategory(Long id);

    Category getCategoryById(Long id);

    Category toggleCategoryStatus(Long id);


}
