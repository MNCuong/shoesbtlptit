package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Service.OrderService;
import com.example.shoes_store.dto.OrderRequestDTO;
import com.example.shoes_store.dto.OrderResponseDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Hiển thị trang đơn hàng của tôi
     */
    @GetMapping
    public String viewOrders(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByUser(loggedInUser);
        model.addAttribute("orders", orders);
        model.addAttribute("user", loggedInUser);

        return "/user/order";
    }

    /**
     * API: Tạo đơn hàng mới
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO request, HttpSession session) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để đặt hàng"
                ));
            }

            if (request.getFullName() == null || request.getFullName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập họ tên"
                ));
            }

            if (request.getAddress() == null || request.getAddress().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập địa chỉ giao hàng"
                ));
            }

            if (request.getPhone() == null || request.getPhone().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập số điện thoại"
                ));
            }

            OrderResponseDTO order = orderService.createOrder(request, loggedInUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt hàng thành công!",
                    "orderCode", order.getOrderCode(),
                    "orderId", order.getId()
            ));

        } catch (RuntimeException e) {
            log.error("Lỗi khi đặt hàng: {}", e.getMessage());
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
     * API: Lấy chi tiết đơn hàng
     */
    @GetMapping("/detail/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId, HttpSession session) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            OrderResponseDTO order = orderService.getOrderById(orderId, loggedInUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", order
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}