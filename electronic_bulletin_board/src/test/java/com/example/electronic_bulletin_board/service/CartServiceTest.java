package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.entity.Ads;
import com.example.electronic_bulletin_board.entity.Cart;
import com.example.electronic_bulletin_board.entity.CartItem;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.CartItemRepository;
import com.example.electronic_bulletin_board.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AdvertisementService advertisementService;

    @Mock
    private AuthService authService;

    private Users user;
    private Cart cart;
    private Ads ad;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new Users();
        user.setId(1);

        cart = new Cart();
        cart.setId(1);
        cart.setUserId(user);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        ad = new Ads();
        ad.setId(1);
        ad.setPrice(BigDecimal.valueOf(100));
    }

    // Тест на попытку создания корзины пользователем, не авторизованным
    @Test
    void testCreateCart_UserNotAuthorized() {
        when(advertisementService.getCurrentUser()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.createCart());
        assertEquals("Пользователь не авторизован", exception.getMessage());
    }

    // Тест на попытку создания корзины, когда она уже существует
    @Test
    void testCreateCart_CartAlreadyExists() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.of(cart));

        Cart result = cartService.createCart();
        assertEquals(cart, result);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // Тест на создание новой корзины
    @Test
    void testCreateCart_NewCart() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.createCart();
        assertNotNull(result);
        assertEquals(user, result.getUserId());
        verify(cartRepository).save(any(Cart.class));
    }

    // Тест на добавление товара в корзину пользователем, не авторизованным
    @Test
    void testAddItemToCart_UserNotAuthorized() {
        when(advertisementService.getCurrentUser()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.addItemToCart(ad.getId()));
        assertEquals("Пользователь не авторизован", exception.getMessage());
    }

    // Тест на добавление товара в ненайденную корзину
    @Test
    void testAddItemToCart_CartNotFound() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.addItemToCart(ad.getId()));
        assertEquals("Корзина не найдена", exception.getMessage());
    }

    // Тест на добавление товара, который не найден
    @Test
    void testAddItemToCart_AdNotFound() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.of(cart));
        when(advertisementService.getAdvertisementById(ad.getId())).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.addItemToCart(ad.getId()));
        assertEquals("Объявление не найдено", exception.getMessage());
    }

    // Тест на успешное добавление товара в корзину
    @Test
    void testAddItemToCart_Success() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.of(cart));
        when(advertisementService.getAdvertisementById(ad.getId())).thenReturn(ad);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());

        CartItem cartItem = cartService.addItemToCart(ad.getId());

        assertNotNull(cartItem);
        assertEquals(BigDecimal.valueOf(100), cart.getTotalPrice());
        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartRepository).save(cart);
    }

    // Тест на получение товаров из корзины пользователем, не авторизованным
    @Test
    void testGetCartItems_UserNotAuthorized() {
        when(advertisementService.getCurrentUser()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.getCartItems());
        assertEquals("Корзина не найдена", exception.getMessage());
    }

    // Тест на получение товаров из ненайденной корзины
    @Test
    void testGetCartItems_CartNotFound() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.getCartItems());
        assertEquals("Корзина не найдена", exception.getMessage());
    }

    // Тест на попытку удаления товара пользователем, не авторизованным
    @Test
    void testRemoveItemFromCart_UserNotAuthorized() {
        when(advertisementService.getCurrentUser()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.removeItemFromCart(1));
        assertEquals("Товар не найден в корзине", exception.getMessage());
    }

    // Тест на попытку удаления несуществующего товара из корзины
    @Test
    void testRemoveItemFromCart_ItemNotFound() {
        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.removeItemFromCart(1));
        assertEquals("Товар не найден в корзине", exception.getMessage());
    }

    // Тест на попытку удаления товара, который не принадлежит корзине
    @Test
    void testRemoveItemFromCart_ItemDoesNotBelongToCart() {
        CartItem cartItem = new CartItem();
        cartItem.setCart(new Cart());

        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(cartItem));

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.removeItemFromCart(1));
        assertEquals("Товар не принадлежит текущей корзине", exception.getMessage());
    }

    // Тест на успешное удаление товара из корзины
    @Test
    void testRemoveItemFromCart_Success() {
        Ads adItem = new Ads();
        adItem.setPrice(BigDecimal.valueOf(100)); // Установите правильную цену

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setAd(adItem);

        // Обновите общую стоимость корзины перед удалением
        cart.setTotalPrice(BigDecimal.valueOf(100));

        when(advertisementService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(cartItem));

        cartService.removeItemFromCart(1);

        assertEquals(BigDecimal.ZERO, cart.getTotalPrice()); // Проверяем, что цена стала 0
        verify(cartItemRepository).delete(cartItem);
        verify(cartRepository).save(cart);
    }

}
