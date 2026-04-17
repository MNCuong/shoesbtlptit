package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.Product;
import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Repo.ProductRepo;
import com.example.shoes_store.Service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepo productRepo;


    @GetMapping
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "redirect:/admin/home";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            return ResponseEntity.ok(productOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    public String addProduct(@RequestParam MultipartFile productImage, @ModelAttribute Product product) {
        productService.saveProduct(productImage, product);
        return "redirect:/admin/home";
    }

    @PostMapping("/edit")
    public String editProduct(@RequestParam MultipartFile productImage, @ModelAttribute Product product) {
        if (product == null) {
            return "redirect:/admin/home";
        }
        productService.updateProduct(productImage, product);
        log.info("product ID {}", product.getId());
        return "redirect:/admin/home";
    }


    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/home";

    }

    @GetMapping("/list-product/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = new ArrayList<>();
        if (categoryId == 0) {
            products = productRepo.findByCategory_Active(true);
        } else {
            products = productRepo.findByCategory_Id(categoryId);
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable Long id) {
        productService.toggleProductStatus(id);
        return "redirect:/admin/home";
    }


}
