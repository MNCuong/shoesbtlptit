package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.*;
import com.example.shoes_store.Repo.EmployeeRepo;
import com.example.shoes_store.Repo.StoreRepo;
import com.example.shoes_store.Repo.SupperlieRepo;
import com.example.shoes_store.Repo.UserRepo;
import com.example.shoes_store.Service.*;
import com.example.shoes_store.dto.AccountDTO;
import com.example.shoes_store.dto.ChangePasswordRequest;
import com.example.shoes_store.dto.OrderResponseDTO;
import com.example.shoes_store.dto.ProductDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SupperlieRepo supperlieRepo;
    @Autowired
    private SupperlieService supperlieService;
    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreRepo storeRepo;

    @GetMapping("/admin")
    public String getAllUsers(Model model) {
        List<User> Users = userService.getAllUsers();
        model.addAttribute("users", Users);
        return "admin/users";
    }


    @ModelAttribute
    public void addUserToModel(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", user);
    }

    @GetMapping("")
    public String homePage() {
        return "/user/index";
    }

    @GetMapping("about")
    public String aboutPage() {
        return "/user/about";
    }

    @GetMapping("/shop")
    public String shopPage(Model model) {
        List<Category> categories = categoryService.getAllWithActive();



        model.addAttribute("categories", categories);
        model.addAttribute("totalProducts", productService.countAll());

        return "/user/shop";
    }
    @GetMapping("/api/products/filter")
    @ResponseBody
    public List<ProductDTO> filterProducts(
            @RequestParam(required = false, defaultValue = "0") Long categoryId,
            @RequestParam(required = false, defaultValue = "all") String gender,
            @RequestParam(required = false, defaultValue = "name-asc") String sort) {

        List<Product> products;

        // Lọc theo danh mục
        if (categoryId == 0) {
            products = productService.getAllProducts();
        } else {
            products = productService.getProductsByCategoryId(categoryId);
        }

        // Lọc theo giới tính
        if (!"all".equals(gender)) {
            products = products.stream()
                    .filter(p -> gender.equals(p.getGender()))
                    .collect(Collectors.toList());
        }

        // Chỉ lấy sản phẩm đang hoạt động
        products = products.stream()
                .filter(Product::isActive)
                .collect(Collectors.toList());

        // Sắp xếp
        switch (sort) {
            case "name-asc":
                products.sort(Comparator.comparing(Product::getName));
                break;
            case "name-desc":
                products.sort(Comparator.comparing(Product::getName).reversed());
                break;
            case "price-asc":
                products.sort(Comparator.comparing(Product::getPrice));
                break;
            case "price-desc":
                products.sort(Comparator.comparing(Product::getPrice).reversed());
                break;
            default:
                products.sort(Comparator.comparing(Product::getName));
        }

        // Chuyển đổi sang DTO
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục (cũ - giữ nguyên để tương thích)
     */
    @GetMapping("/admin/products/list-product/{categoryId}")
    @ResponseBody
    public List<ProductDTO> getProductsByCategory(@PathVariable Long categoryId,
                                                  @RequestParam(required = false, defaultValue = "all") String gender) {
        List<Product> products;

        if (categoryId == 0) {
            products = productService.getAllProducts();
        } else {
            products = productService.getProductsByCategoryId(categoryId);
        }

        if (!"all".equals(gender)) {
            products = products.stream()
                    .filter(p -> gender.equals(p.getGender()))
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Product sang DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setGender(product.getGender());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setActive(product.isActive());
        dto.setDescription(product.getDescription());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        return dto;
    }

    @GetMapping("contact")
    public String contactPage() {
        return "/user/contact";
    }
    @GetMapping("/update-use")
    public String showUpdateUserForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        return "/user/updateUserInfo";
    }

    @PostMapping("/user/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                             HttpSession session,
                             Model model) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return "redirect:/login";
            }

            // Cập nhật thông tin
            loggedInUser.setFullname(user.getFullname());
            loggedInUser.setEmail(user.getEmail());
            loggedInUser.setAddress(user.getAddress());
            loggedInUser.setPhone(user.getPhone());

            // Xử lý upload avatar
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                String uploadDir = "uploads/avatars/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Files.copy(avatarFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
//                loggedInUser.set("/uploads/avatars/" + fileName);
            }

            userRepo.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);

            model.addAttribute("message", "Cập nhật thông tin thành công!");
            model.addAttribute("user", loggedInUser);

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "/user/updateUserInfo";
    }
    @GetMapping("/admin/home")
    public String homeAdminPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            log.info("Không tìm thấy người dùng đăng nhập trong session. Chuyển hướng về trang đăng nhập.");
            return "redirect:/login";
        }

        if (!loggedInUser.getRole().equals("ADMIN") && !loggedInUser.getRole().equals("STAFF")) {

            log.info("Người dùng {} là {}. Chuyển hướng về trang lỗi.", loggedInUser.getUsername(), loggedInUser.getRole());
            model.addAttribute("status", 403);
            model.addAttribute("error", "Access Denied");
            model.addAttribute("message", "Bạn không có quyền truy cập vào trang admin");

            return "error";
        }

        List<Category> categories = categoryService.getAll();
        List<Category> categoriesActive = categoryService.getAllWithActive();
        List<Product> products = productService.getAllProducts();
        List<User> users = userService.getAllUsers();
        List<Store> stores = storeRepo.findAll();
        List<Employee> employees = employeeRepo.findAll();
        List<Supplier> suppliers = supperlieRepo.findAll();


        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        List<Integer> years = new ArrayList<>();
        for (int y = currentYear; y >= currentYear - 5; y--) {
            years.add(y);
        }

        model.addAttribute("years", years);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("categories", categories);
        model.addAttribute("categoriesActive", categoriesActive);
        model.addAttribute("products", products);
        model.addAttribute("users", users);
        model.addAttribute("stores", stores);
        model.addAttribute("suppliers", supperlieService.getAll());
        model.addAttribute("employees", employeeService.getAll());
        log.info("Người dùng ADMIN: " + loggedInUser.getUsername());
        model.addAttribute("user", loggedInUser);
        return "/admin/index";
    }


    @GetMapping("/{id}")
    public String getUserDetails(@PathVariable Long id, Model model) {
        User User = userService.getUserById(id);
        if (User != null) {
            model.addAttribute("user", User);
        }
        return "admin/user-details";
    }

    @PostMapping("/api/users/add")
    public String addUser(@RequestBody AccountDTO accountDTO) {
        User user = new User();
        user.setUsername(accountDTO.getUsername());
        user.setPassword(accountDTO.getPassword());
        user.setRole(accountDTO.getRole());
        user.setEmail(accountDTO.getEmail());
        user.setFullname(accountDTO.getFullname());
        user.setCart(null);
        user.setPhone("");
        userRepo.save(user);
        return "redirect:/admin/home";
    }

    @PostMapping("/api/users/update/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody AccountDTO accountDTO) {
        User user = userService.getUserById(id);
        user.setUsername(accountDTO.getUsername());
        user.setPassword(accountDTO.getPassword());
        user.setRole(accountDTO.getRole());
        user.setEmail(accountDTO.getEmail());
        user.setFullname(accountDTO.getFullname());
        userRepo.save(user);
        return "redirect:/admin/home";
    }

    @DeleteMapping("/api/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        return "redirect:/admin/home";
    }
//
//    @GetMapping("/shop")
//    public String shopPage(HttpSession session, Model model) {
//        List<Category> categoriesActive = categoryService.getAllWithActive();
//        List<Product> products = productService.getAllProductsWithCateActive();
//        model.addAttribute("categories", categoriesActive);
//        model.addAttribute("products", products);
//
//        User loggedInUser = (User) session.getAttribute("loggedInUser");
//        model.addAttribute("user", loggedInUser);
//        int cartItemQuantity = cartItemService.getQuantity(loggedInUser);
//        log.info("cartItemQuantity {}", cartItemQuantity);
//        model.addAttribute("cartItemQuantity", cartItemQuantity);
//
//        return "/user/shop";
//    }
//
    @GetMapping("/product/{id}")
    public String shopSinglePage(HttpSession session, Model model, @PathVariable Long id) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
//        int cartItemQuantity = cartItemService.getQuantity(loggedInUser);
//        log.info("cartItemQuantity {}", cartItemQuantity);
//        model.addAttribute("cartItemQuantity", cartItemQuantity);
        Product p = productService.getProductById(id).get();
        model.addAttribute("product", p);
        model.addAttribute("category", p.getCategory());
        model.addAttribute("user", loggedInUser);
        return "/user/shop-single";
    }

    @GetMapping("/admin/orders/all")
    @ResponseBody
    public ResponseEntity<?> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(Map.of("success", true, "data", orders));
    }

    @GetMapping("/admin/orders/{id}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", order));
    }

    @PutMapping("/admin/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công"));
    }
    @PostMapping("/api/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (!request.getOldPassword().equals(loggedInUser.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu cũ không chính xác"));
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu xác nhận không khớp"));
        }
        userService.changePassword(request, loggedInUser);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công"));
    }
//
//    @PostMapping("/api/check-email")
//    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> req) {
//        String email = req.get("email");
//        Optional<User> userOpt = userService.getByEmail(email);
//        if (userOpt.isPresent()) {
//            return ResponseEntity.ok(Map.of("success", true, "message", "Email hợp lệ. Mời bạn nhập mật khẩu mới"));
//        }
//        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email không tồn tại"));
//    }

    @PostMapping("/api/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String newPassword = req.get("newPassword");
        String confirmPassword = req.get("confirmPassword");
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu xác nhận không khớp"));
        }
        Optional<User> userOpt = userService.getByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy tài khoản"));
        }
        User user = userOpt.get();
        user.setPassword(newPassword);
        userService.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đặt lại mật khẩu thành công"));
    }
}
