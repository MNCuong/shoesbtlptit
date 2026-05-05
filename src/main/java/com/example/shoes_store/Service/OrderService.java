package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.*;
import com.example.shoes_store.Repo.*;
import com.example.shoes_store.dto.OrderDetailResponseDTO;
import com.example.shoes_store.dto.OrderRequestDTO;
import com.example.shoes_store.dto.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepo productRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request, User user) {
        log.info("=== CREATE ORDER ===");
        log.info("User: {}", user.getUsername());

        // Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng");
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setOrderCode("ORD-" + System.currentTimeMillis());
        order.setUser(user);
        order.setFullName(request.getFullName());
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("PENDING");
        order.setNote("");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        // Tạo chi tiết đơn hàng từ giỏ
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(cartItem.getPrice());
            detail.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            detail.setSize(cartItem.getSize());
            detail.setGender(product.getGender());

            orderDetails.add(detail);
            totalAmount = totalAmount.add(detail.getTotalPrice());

            // Cập nhật tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(orderDetails);

        // Lưu đơn hàng
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteByCart(cart);

        log.info("Order created successfully! Order code: {}", savedOrder.getOrderCode());
        log.info("Total amount: {}", totalAmount);

        return convertToResponseDTO(savedOrder);
    }

    public List<OrderResponseDTO> getOrdersByUser(User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        return convertToResponseDTO(order);
    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setFullName(order.getFullName());
        dto.setAddress(order.getAddress());
        dto.setPhone(order.getPhone());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderDetailResponseDTO> detailDTOs = order.getOrderDetails().stream()
                .map(detail -> {
                    OrderDetailResponseDTO detailDTO = new OrderDetailResponseDTO();
                    detailDTO.setId(detail.getId());
                    detailDTO.setProductId(detail.getProduct().getId());
                    detailDTO.setProductName(detail.getProduct().getName());
                    detailDTO.setProductImage(detail.getProduct().getImageUrl());
                    detailDTO.setQuantity(detail.getQuantity());
                    detailDTO.setPrice(detail.getPrice());
                    detailDTO.setTotalPrice(detail.getTotalPrice());
                    detailDTO.setSize(detail.getSize());
                    detailDTO.setGender(detail.getGender());
                    return detailDTO;
                })
                .collect(Collectors.toList());

        dto.setOrderDetails(detailDTOs);
        return dto;
    }

    /**
     * Lấy tất cả đơn hàng (cho admin)
     */
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng theo ID (cho admin - không kiểm tra quyền)
     */
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return convertToResponseDTO(order);
    }

    /**
     * Lấy đơn hàng theo ID và user (cho user)
     */
    public OrderResponseDTO getOrderByIdAndUser(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        return convertToResponseDTO(order);
    }



    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatus) {
        log.info("Updating order {} status to: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Validate status
        if (!isValidStatus(newStatus)) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // Nếu hủy đơn hàng, hoàn lại số lượng sản phẩm vào kho
        if ("CANCELLED".equals(newStatus) && !"CANCELLED".equals(oldStatus)) {
            restoreProductStock(order);
        }

        // Nếu đã giao hàng thành công
        if ("DELIVERED".equals(newStatus) && !"DELIVERED".equals(oldStatus)) {
            log.info("Order {} has been delivered", order.getOrderCode());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", order.getOrderCode(), oldStatus, newStatus);

        return convertToResponseDTO(savedOrder);
    }

    /**
     * Hoàn lại số lượng sản phẩm vào kho khi hủy đơn
     */
    private void restoreProductStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            product.setStockQuantity(product.getStockQuantity() + detail.getQuantity());
            productRepository.save(product);
            log.info("Restored {} units of product {} back to stock", detail.getQuantity(), product.getName());
        }
    }

    /**
     * Kiểm tra trạng thái hợp lệ
     */
    private boolean isValidStatus(String status) {
        return status != null && (
                "PENDING".equals(status) ||
                        "CONFIRMED".equals(status) ||
                        "SHIPPING".equals(status) ||
                        "DELIVERED".equals(status) ||
                        "CANCELLED".equals(status)
        );
    }

}