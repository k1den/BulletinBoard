package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.entity.Cart;
import com.example.electronic_bulletin_board.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);
}