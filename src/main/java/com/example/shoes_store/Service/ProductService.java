package com.example.shoes_store.Service;


import com.example.shoes_store.Entity.Product;
import com.example.shoes_store.Repo.ProductRepo;
import com.example.shoes_store.dto.ProductDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getAllProducts();

    List<Product> getAllProductsWithCateActive();

    Product saveProduct(MultipartFile productImage, Product product);

    Optional<Product> getProductById(Long id);

    Product updateProduct(MultipartFile productImage, Product product);

    List<Product> getProductsByCategory(Long categoryId);

    Product toggleProductStatus(Long id);

    Product save(ProductDTO product);
    Product update(ProductDTO product);
    void deleteProduct(Long id);

}
