package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Cart;
import com.example.shoes_store.Entity.CartItem;
import com.example.shoes_store.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProductIdAndSize(Cart cart, Long productId, String size);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteByCart(@Param("cart") Cart cart);
}