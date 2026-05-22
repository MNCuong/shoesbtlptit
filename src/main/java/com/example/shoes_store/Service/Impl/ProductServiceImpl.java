package com.example.shoes_store.Service.Impl;


import com.example.shoes_store.Entity.Category;
import com.example.shoes_store.Entity.Product;
import com.example.shoes_store.Repo.*;
import com.example.shoes_store.Service.ProductService;
import com.example.shoes_store.dto.ProductDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ExportReceiptDetailRepo exportReceiptDetailRepo;

    @Autowired
    private ImportReceiptDetailRepo importReceiptDetailRepo;

    @Autowired
    private OrderDetailRepository orderDetailRepo;

    @Autowired
    private CartItemRepository cartItemRepo;

    @Value("${upload.path}")
    private String uploadPath;
    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public long countAll() {
        return productRepo.count();
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepo.findByCategory_Id(categoryId);
    }

    @Override
    public List<Product> getAllProductsWithCateActive() {
        return productRepo.findByCategory_Active(true);
    }

    @Override
    public Product saveProduct(MultipartFile productImage, Product product) {
        if (!productImage.isEmpty()) {
            String originalFileName = productImage.getOriginalFilename();
            product.setImageUrl(originalFileName);
            Path uploadPath = Paths.get("src/main/resources/static/assets/img/products/");
            try {
                Files.createDirectories(uploadPath);
                assert originalFileName != null;
                productImage.transferTo(uploadPath.resolve(originalFileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return productRepo.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // 1. Xóa export_receipt_details (QUAN TRỌNG NHẤT)
        try {
            exportReceiptDetailRepo.deleteByProductId(id);
            log.info("Đã xóa export_receipt_details cho product ID: {}", id);
        } catch (Exception e) {
            log.warn("Không có export_receipt_details cho product ID: {}", id);
        }

        // 2. Xóa import_receipt_details
        try {
            importReceiptDetailRepo.deleteByProductId(id);
            log.info("Đã xóa import_receipt_details cho product ID: {}", id);
        } catch (Exception e) {
            log.warn("Không có import_receipt_details cho product ID: {}", id);
        }

        // 3. Xóa order_details
        try {
            orderDetailRepo.deleteByProductId(id);
            log.info("Đã xóa order_details cho product ID: {}", id);
        } catch (Exception e) {
            log.warn("Không có order_details cho product ID: {}", id);
        }

        // 4. Xóa cart_items
        try {
            cartItemRepo.deleteByProductId(id);
            log.info("Đã xóa cart_items cho product ID: {}", id);
        } catch (Exception e) {
            log.warn("Không có cart_items cho product ID: {}", id);
        }

        // 5. Cuối cùng xóa sản phẩm
        productRepo.deleteById(id);
        log.info("Đã xóa sản phẩm ID: {}", id);
    }
    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepo.findById(id);
    }

    @Override
    public Product updateProduct(MultipartFile productImage, Product product) {
        Product existingProduct = productRepo.findById(product.getId()).orElseThrow(() -> new RuntimeException("Product not found"));

        if (existingProduct != null) {
            if (!productImage.isEmpty()) {
                String originalFileName = productImage.getOriginalFilename();
                product.setImageUrl(originalFileName);
                Path uploadPath = Paths.get("src/main/resources/static/assets/img/products/");
                try {
                    Files.createDirectories(uploadPath);
                    assert originalFileName != null;
                    productImage.transferTo(uploadPath.resolve(originalFileName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                product.setImageUrl(existingProduct.getImageUrl());

            }
            return productRepo.save(product);
        } else {
            throw new EntityNotFoundException("Product with ID " + product.getId() + " not found");
        }
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepo.findByCategory_Id(categoryId);
    }

    @Override
    public Product toggleProductStatus(Long id) {
        Product category = productRepo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Category with ID " + id + " not found"));
        category.setActive(!category.isActive());
        return productRepo.save(category);
    }


    public Product save(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setDescription(productDTO.getDescription());
        product.setFeatures(productDTO.getFeatures());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setGender(productDTO.getGender());
        product.setAgeGroup(productDTO.getAgeGroup());
        product.setActive(productDTO.isActive());

        // Xử lý ảnh
        if (productDTO.getImage() != null && !productDTO.getImage().isEmpty()) {
            String imageUrl = saveImage(productDTO.getImage());
            product.setImageUrl(imageUrl);
        }

        // Set category
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepo.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }

        return productRepo.save(product);
    }

    public Product update(ProductDTO productDTO) {
        Product product = productRepo.findById(productDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setDescription(productDTO.getDescription());
        product.setFeatures(productDTO.getFeatures());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setGender(productDTO.getGender());
        product.setAgeGroup(productDTO.getAgeGroup());
        product.setActive(productDTO.isActive());

        // Xử lý ảnh: nếu có ảnh mới thì upload, không thì giữ ảnh cũ
        if (productDTO.getImage() != null && !productDTO.getImage().isEmpty()) {
            String imageUrl = saveImage(productDTO.getImage());
            product.setImageUrl(imageUrl);
        }
        // Nếu không có ảnh mới và cũng không có ảnh cũ thì xóa ảnh
        else if (productDTO.getExistingImageUrl() == null || productDTO.getExistingImageUrl().isEmpty()) {
            product.setImageUrl(null);
        }

        // Update category
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepo.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }

        return productRepo.save(product);
    }

    private String saveImage(MultipartFile file) {
        try {
            // Lấy đường dẫn tuyệt đối đến thư mục gốc dự án
            String projectPath = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectPath, "uploads", "products");

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file duy nhất (tránh trùng)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Lưu file vào thư mục
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            // Trả về đường dẫn để lưu vào database
            return "/uploads/products/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

}
