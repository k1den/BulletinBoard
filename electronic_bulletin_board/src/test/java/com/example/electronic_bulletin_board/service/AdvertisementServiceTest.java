package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.AdvertisementDto;
import com.example.electronic_bulletin_board.entity.Ads;
import com.example.electronic_bulletin_board.entity.Categories;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.AdvertisementRepository;
import com.example.electronic_bulletin_board.repository.CategoryRepository;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository adsRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AdvertisementService advertisementService;

    private Users regularUser;
    private Users adminUser;
    private Categories category;
    private Ads ad;

    @BeforeEach
    void setUp() {
        regularUser = new Users();
        regularUser.setId(1);
        regularUser.setLogin("user");
        regularUser.setRole("Пользователь");

        adminUser = new Users();
        adminUser.setId(2);
        adminUser.setLogin("admin");
        adminUser.setRole("Администратор");

        category = new Categories();
        category.setId(1);
        category.setTitle("Электроника");

        ad = new Ads();
        ad.setId(1);
        ad.setTitle("Ноутбук");
        ad.setDescription("Новый");
        ad.setPrice(BigDecimal.valueOf(1000));
        ad.setIdCategory(category);
        ad.setIdUsers(regularUser);
    }

    // Тест добавления объявления (успешный сценарий)
    @Test
    void addAdvertisement_Success() {
        AdvertisementDto dto = new AdvertisementDto();
        dto.setTitle("Ноутбук");
        dto.setDescription("Новый");
        dto.setPrice(BigDecimal.valueOf(1000));
        dto.setIdCategory(1);

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(regularUser);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(adsRepository.save(any(Ads.class))).thenReturn(ad);

        Ads result = advertisementService.addAdvertisement(dto);

        assertNotNull(result);
        assertEquals("Ноутбук", result.getTitle());
        verify(adsRepository, times(1)).save(any(Ads.class));
    }

    // Тест добавления объявления (неавторизованный пользователь)
    @Test
    void addAdvertisement_UnauthorizedUser_ThrowsException() {
        when(authService.isSessionActive()).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                advertisementService.addAdvertisement(new AdvertisementDto())
        );
    }

    // Тест удаления объявления (пользователь = владелец)
    @Test
    void deleteAdvertisement_ByOwner_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(regularUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        advertisementService.deleteAdvertisement(1);

        verify(adsRepository, times(1)).deleteById(1);
    }

    // Тест удаления объявления (администратор)
    @Test
    void deleteAdvertisement_ByAdmin_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(userRepository.findByLogin("admin")).thenReturn(adminUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        advertisementService.deleteAdvertisement(1);

        verify(adsRepository, times(1)).deleteById(1);
    }

    // Тест удаления объявления (недостаточно прав)
    @Test
    void deleteAdvertisement_NoPermissions_ThrowsException() {
        Users anotherUser = new Users();
        anotherUser.setId(3);
        anotherUser.setRole("Пользователь");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("another");
        when(userRepository.findByLogin("another")).thenReturn(anotherUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        assertThrows(RuntimeException.class, () ->
                advertisementService.deleteAdvertisement(1)
        );
    }

    // Тест обновления объявления
    @Test
    void updateAdvertisement_Success() {
        AdvertisementDto dto = new AdvertisementDto();
        dto.setTitle("Обновленный ноутбук");
        dto.setDescription("Б/У");
        dto.setPrice(BigDecimal.valueOf(800));
        dto.setIdCategory(1);

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(regularUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(adsRepository.save(any(Ads.class))).thenReturn(ad);

        Ads result = advertisementService.updateAdvertisement(1, dto);

        assertEquals("Обновленный ноутбук", result.getTitle());
        verify(adsRepository, times(1)).save(ad);
    }

    // Тест проверки владельца объявления
    @Test
    void isAdOwner_ReturnsTrue() {
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        assertTrue(advertisementService.isAdOwner(1, 1));
    }
}