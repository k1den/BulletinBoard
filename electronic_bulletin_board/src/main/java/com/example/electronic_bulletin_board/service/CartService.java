package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.entity.Ads;
import com.example.electronic_bulletin_board.entity.Cart;
import com.example.electronic_bulletin_board.entity.CartItem;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.CartItemRepository;
import com.example.electronic_bulletin_board.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AdvertisementService advertisementService;

    @Autowired
    private AuthService authService;

    // Создание корзины для текущего пользователя
    public Cart createCart() {
        Users currentUser = advertisementService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        Cart existingCart = cartRepository.findByUserId(currentUser ).orElse(null);
        if (existingCart != null) {
            return existingCart;
        }

        Cart cart = new Cart();
        cart.setUserId(currentUser);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }
    @Transactional
    // Добавление товара в корзину
    public CartItem addItemToCart(Integer adId) {
        Users currentUser  = advertisementService.getCurrentUser ();
        if (currentUser  == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }
        System.out.println("Пользователь авторизован: " + currentUser .getId());

        Cart cart = cartRepository.findByUserId(currentUser )
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));
        System.out.println("Корзина найдена: " + cart.getId());

        Ads ad = advertisementService.getAdvertisementById(adId);
        if (ad == null) {
            throw new RuntimeException("Объявление не найдено");
        }
        System.out.println("Объявление найдено: " + ad.getId());

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setAd(ad);

        // Обновление общей стоимости корзины
        cart.setTotalPrice(cart.getTotalPrice().add(ad.getPrice()));
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return cartItemRepository.save(cartItem);
    }

    // Получение всех товаров в корзине
    public List<CartItem> getCartItems() {
        Users currentUser = advertisementService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));
        return cartItemRepository.findByCart(cart);
    }

    // Удаление товара из корзины
    public void removeItemFromCart(Integer itemId) {
        Users currentUser = advertisementService.getCurrentUser();
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Товар не найден в корзине"));

        Cart cart = cartRepository.findByUserId(currentUser)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        if (!cartItem.getCart().equals(cart)) {
            throw new RuntimeException("Товар не принадлежит текущей корзине");
        }

        // Обновление общей стоимости корзины
        cart.setTotalPrice(cart.getTotalPrice().subtract(cartItem.getAd().getPrice()));
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        cartItemRepository.delete(cartItem);
    }
}