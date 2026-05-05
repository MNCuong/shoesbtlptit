package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.*;
import com.example.shoes_store.Repo.CartItemRepository;
import com.example.shoes_store.Repo.CartRepository;
import com.example.shoes_store.Repo.ProductRepo;
import com.example.shoes_store.Repo.UserRepo;
import com.example.shoes_store.dto.CartItemResponseDTO;
import com.example.shoes_store.dto.CartRequestDTO;
import com.example.shoes_store.dto.CartResponseDTO;
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
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepo productRepository;
    private final UserRepo userRepo;

    /**
     * Lấy hoặc tạo giỏ hàng cho user
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Transactional
    public CartResponseDTO addToCart(CartRequestDTO request, User user) {
        // Kiểm tra user
        if (user == null) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }

        // Kiểm tra sản phẩm
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra size
        if (request.getSize() == null || request.getSize().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn size sản phẩm");
        }

        // Kiểm tra số lượng tồn kho theo size
        int availableStock = getStockBySize(product, request.getSize());
        if (availableStock < request.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm size " + request.getSize() + " không đủ. Chỉ còn " + availableStock);
        }

        // Lấy hoặc tạo giỏ hàng
        Cart cart = getOrCreateCart(user);

        // Tìm sản phẩm đã có trong giỏ (cùng product và size)
        CartItem existingItem = cartItemRepository.findByCartAndProductIdAndSize(cart, request.getProductId(), request.getSize()).orElse(null);

        if (existingItem != null) {
            // Cập nhật số lượng
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (availableStock < newQuantity) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho. Còn " + availableStock);
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUser(user);
            cartItemRepository.save(existingItem);
        } else {
            // Thêm mới
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setSize(request.getSize());
            newItem.setPrice(product.getPrice());
            cart.getItems().add(newItem);
            newItem.setUser(user);
            cartItemRepository.save(newItem);
        }

        log.info("Added to cart: User={}, Product={}, Size={}, Quantity={}",
                user.getUsername(), product.getName(), request.getSize(), request.getQuantity());

        return convertToDTO(cart);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @Transactional
    public CartResponseDTO updateQuantity(Long cartItemId, Integer quantity, User user) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        // Kiểm tra quyền sở hữu
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa sản phẩm này");
        }

        if (quantity <= 0) {
            // Xóa sản phẩm nếu số lượng <= 0
            cartItemRepository.delete(cartItem);
        } else {
            // Kiểm tra tồn kho
            int availableStock = getStockBySize(cartItem.getProduct(), cartItem.getSize());
            if (availableStock < quantity) {
                throw new RuntimeException("Số lượng không đủ. Chỉ còn " + availableStock);
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        Cart cart = cartItem.getCart();
        return convertToDTO(cart);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Transactional
    public CartResponseDTO removeFromCart(Long cartItemId, User user) {
        System.out.println("=== REMOVE FROM CART SERVICE ===");
        System.out.println("cartItemId: " + cartItemId);

        // Tìm cartItem
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        System.out.println("Found CartItem - Product: " + cartItem.getProduct().getName());
        System.out.println("Cart ID: " + cartItem.getCart().getId());

        // Kiểm tra quyền
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm này");
        }

        Cart cart = cartItem.getCart();

        cart.getItems().remove(cartItem);
        System.out.println("Removed from collection, new size: " + cart.getItems().size());

        // Xóa khỏi database
        cartItemRepository.delete(cartItem);
        cartItemRepository.flush();

        System.out.println("CartItem deleted and flushed!");
        System.out.println("Cart items count after delete: " + cart.getItems().size());

        return convertToDTO(cart);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);
        log.info("Cleared cart: User={}", user.getUsername());
    }

    /**
     * Lấy thông tin giỏ hàng
     */
    public CartResponseDTO getCart(User user) {
        if (user == null) {
            return null;
        }
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return null;
        }
        return convertToDTO(cart);
    }

    /**
     * Lấy số lượng sản phẩm trong giỏ
     */
    public int getTotalQuantity(User user) {
        if (user == null) return 0;
        Cart cart = cartRepository.findByUser(user).orElse(null);
        return cart != null ? cart.getTotalQuantity() : 0;
    }

    /**
     * Lấy tổng tiền giỏ hàng
     */
    public BigDecimal getTotalAmount(User user) {
        if (user == null) return BigDecimal.ZERO;
        Cart cart = cartRepository.findByUser(user).orElse(null);
        return cart != null ? cart.getTotalPrice() : BigDecimal.ZERO;
    }

    /**
     * Lấy số lượng tồn kho theo size (cần implement theo logic của bạn)
     */
    private int getStockBySize(Product product, String size) {
        // TODO: Implement stock by size logic
        // Nếu sản phẩm có bảng stock riêng theo size thì lấy từ đó
        // Hiện tại trả về stockQuantity của product
        return product.getStockQuantity();
    }

    /**
     * Chuyển đổi Cart sang DTO
     */
    private CartResponseDTO convertToDTO(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setTotalQuantity(cart.getTotalQuantity());
        dto.setItemCount(cart.getItems().size());

        List<CartItemResponseDTO> itemDTOs = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            CartItemResponseDTO itemDTO = new CartItemResponseDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setProductImage(item.getProduct().getImageUrl());
            itemDTO.setProductPrice(item.getPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemDTO.setSize(item.getSize());
            itemDTO.setGender(item.getProduct().getGender());
            itemDTO.setStockQuantity(item.getProduct().getStockQuantity());

            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);

        return dto;
    }
}