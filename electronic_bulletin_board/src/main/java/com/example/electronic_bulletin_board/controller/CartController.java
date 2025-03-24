package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.model.Cart;
import com.example.electronic_bulletin_board.model.CartItem;
import com.example.electronic_bulletin_board.model.Users;
import com.example.electronic_bulletin_board.repository.CartRepository;
import com.example.electronic_bulletin_board.service.AdvertisementService;
import com.example.electronic_bulletin_board.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "API для управления корзиной", description = "Предоставляет методы для работы с корзиной пользователя")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AdvertisementService advertisementService;

    @Autowired
    private CartRepository cartRepository;

    // Создание корзины
    @PostMapping("/create")
    @Operation(summary = "Создание корзины", description = "Создает новую корзину для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно создана"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<Cart> createCart() {
        Cart cart = cartService.createCart();
        return ResponseEntity.ok(cart);
    }

    // Добавление товара в корзину
    @PostMapping("/{cartId}/add/{adId}")
    @Operation(summary = "Добавление товара в корзину", description = "Добавляет товар в корзину по идентификаторам корзины и объявления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно добавлен в корзину"),
            @ApiResponse(responseCode = "404", description = "Корзина или объявление не найдены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<CartItem> addItemToCart(@PathVariable Integer adId) {
        CartItem cartItem = cartService.addItemToCart(adId);
        return ResponseEntity.ok(cartItem);
    }

    // Получение всех товаров в корзине
    @GetMapping("/items")
    @Operation(summary = "Получение товаров в корзине", description = "Возвращает список всех товаров в корзине текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список товаров успешно получен"),
            @ApiResponse(responseCode = "404", description = "Корзина не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<List<CartItem>> getCartItems() {
        Users currentUser = advertisementService.getCurrentUser();
        List<CartItem> cartItems = cartService.getCartItems();
        return ResponseEntity.ok(cartItems);
    }

    // Удаление товара из корзины
    @DeleteMapping("/{cartId}/remove/{itemId}")
    @Operation(summary = "Удаление товара из корзины", description = "Удаляет товар из корзины по идентификаторам корзины и товара")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Товар успешно удален из корзины"),
            @ApiResponse(responseCode = "404", description = "Корзина или товар не найдены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Integer itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cartId")
    @Operation(summary = "Получение идентификатора корзины", description = "Возвращает идентификатор корзины текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Идентификатор корзины успешно получен"),
            @ApiResponse(responseCode = "404", description = "Корзина не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<Integer> getCartId() {
        Users currentUser = advertisementService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));
        return ResponseEntity.ok(cart.getId());
    }
}