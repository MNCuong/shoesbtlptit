package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Service.CartService;
import com.example.shoes_store.dto.CartItemResponseDTO;
import com.example.shoes_store.dto.CartRequestDTO;
import com.example.shoes_store.dto.CartResponseDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Hiển thị trang giỏ hàng - SỬA: path là "/" hoặc để trống
     */
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Lấy giỏ hàng
        CartResponseDTO cart = cartService.getCart(loggedInUser);

        List<CartItemResponseDTO> cartItems = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
            cartItems = cart.getItems();
            totalQuantity = cart.getTotalQuantity();
            totalAmount = cart.getTotalPrice();


        } else {
            System.out.println("Cart is empty or null!");
            System.out.println("Cart object: " + cart);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", loggedInUser);

        // Thêm debug attribute để hiển thị trong view
        model.addAttribute("debugCartItems", cartItems);
        model.addAttribute("debugCartItemsCount", cartItems.size());

        // Lưu vào session
        session.setAttribute("cartItemQuantity", totalQuantity);

        return "/user/cart";
    }

    /**
     * API: Thêm vào giỏ hàng
     */
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody CartRequestDTO request, HttpSession session) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để thêm vào giỏ hàng"
                ));
            }

            // Kiểm tra thông tin bắt buộc
            if (request.getProductId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng chọn sản phẩm"
                ));
            }

            if (request.getSize() == null || request.getSize().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng chọn size sản phẩm"
                ));
            }

            if (request.getQuantity() == null || request.getQuantity() < 1) {
                request.setQuantity(1);
            }

            CartResponseDTO cart = cartService.addToCart(request, loggedInUser);

            if (cart == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không thể thêm sản phẩm vào giỏ hàng"
                ));
            }

            session.setAttribute("cartItemQuantity", cart.getTotalQuantity());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thêm sản phẩm vào giỏ hàng",
                    "cartItemCount", cart.getTotalQuantity()
            ));

        } catch (RuntimeException e) {
            log.error("Lỗi khi thêm vào giỏ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi không xác định: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra, vui lòng thử lại sau"
            ));
        }
    }

    /**
     * API: Cập nhật số lượng sản phẩm trong giỏ
     */
    @PutMapping("/cart/update/{cartItemId}")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            HttpSession session) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            if (quantity == null || quantity < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số lượng không hợp lệ"
                ));
            }

            CartResponseDTO cart = cartService.updateQuantity(cartItemId, quantity, loggedInUser);

            int totalQuantity = cart != null ? cart.getTotalQuantity() : 0;
            BigDecimal totalAmount = cart != null ? cart.getTotalPrice() : BigDecimal.ZERO;

            session.setAttribute("cartItemQuantity", totalQuantity);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật thành công",
                    "totalQuantity", totalQuantity,
                    "totalAmount", totalAmount
            ));

        } catch (RuntimeException e) {
            log.error("Lỗi khi cập nhật số lượng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi không xác định: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra, vui lòng thử lại sau"
            ));
        }
    }

    /**
     * API: Xóa một sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/cart/remove/{cartItemId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId, HttpSession session) {
        System.out.println("=== REMOVE FROM CART CONTROLLER ===");
        System.out.println("Received cartItemId: " + cartItemId);

        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                System.out.println("User not logged in!");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            System.out.println("User: " + loggedInUser.getUsername() + " (ID: " + loggedInUser.getId() + ")");

            CartResponseDTO cart = cartService.removeFromCart(cartItemId, loggedInUser);

            int totalQuantity = cart != null ? cart.getTotalQuantity() : 0;
            BigDecimal totalAmount = cart != null ? cart.getTotalPrice() : BigDecimal.ZERO;

            session.setAttribute("cartItemQuantity", totalQuantity);

            System.out.println("Remove successful!");
            System.out.println("New total quantity: " + totalQuantity);
            System.out.println("New total amount: " + totalAmount);
            System.out.println("=== REMOVE FROM CART CONTROLLER END ===");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xóa sản phẩm khỏi giỏ hàng",
                    "totalQuantity", totalQuantity,
                    "totalAmount", totalAmount
            ));

        } catch (RuntimeException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * API: Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(HttpSession session) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            cartService.clearCart(loggedInUser);
            session.setAttribute("cartItemQuantity", 0);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xóa toàn bộ giỏ hàng"
            ));

        } catch (Exception e) {
            log.error("Lỗi khi xóa giỏ hàng: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * API: Lấy số lượng sản phẩm trong giỏ hàng
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        int count = cartService.getTotalQuantity(loggedInUser);
        return ResponseEntity.ok(Map.of("count", count));
    }
}