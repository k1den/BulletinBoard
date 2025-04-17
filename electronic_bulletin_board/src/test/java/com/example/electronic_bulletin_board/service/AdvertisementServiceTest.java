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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository adsRepository;

    @Mock
    private CategoryRepository categoriesRepository;

    @Mock
    private UserRepository usersRepository;

    @Mock
    private AuthService authService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AdvertisementService advertisementService;

    private Users regularUser;
    private Users adminUser;
    private Users guestUser;
    private Categories category;
    private Ads ad;
    private AdvertisementDto adDto;

    @BeforeEach
    void setUp() {
        regularUser = new Users();
        regularUser.setId(1);
        regularUser.setLogin("user1");
        regularUser.setRole("Пользователь");

        adminUser = new Users();
        adminUser.setId(2);
        adminUser.setLogin("admin");
        adminUser.setRole("Администратор");

        guestUser = new Users();
        guestUser.setId(3);
        guestUser.setLogin("guest");
        guestUser.setRole("Гость");

        category = new Categories();
        category.setId(1);
        category.setTitle("Электроника");

        ad = new Ads();
        ad.setId(1);
        ad.setTitle("Телефон");
        ad.setDescription("Новый телефон");
        ad.setPrice(BigDecimal.valueOf(10000));
        ad.setDate(new Date());
        ad.setPhoto(new byte[]{1, 2, 3});
        ad.setIdCategory(category);
        ad.setIdUsers(regularUser);

        adDto = new AdvertisementDto();
        adDto.setTitle("Телефон");
        adDto.setDescription("Новый телефон");
        adDto.setPrice(BigDecimal.valueOf(10000));
        adDto.setPhoto(new byte[]{1, 2, 3});
        adDto.setIdCategory(1);
    }

    @Test
    void addAdvertisement_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(usersRepository.findByLogin("user1")).thenReturn(regularUser);
        when(categoriesRepository.findById(1)).thenReturn(Optional.of(category));
        when(adsRepository.save(any(Ads.class))).thenReturn(ad);

        Ads result = advertisementService.addAdvertisement(adDto);

        assertNotNull(result);
        assertEquals("Телефон", result.getTitle());
        verify(adsRepository, times(1)).save(any(Ads.class));
    }

    @Test
    void addAdvertisement_UserNotLoggedIn_ThrowsException() {
        when(authService.isSessionActive()).thenReturn(false);

        assertThrows(RuntimeException.class, () -> advertisementService.addAdvertisement(adDto));
    }

    @Test
    void addAdvertisement_GuestUser_ThrowsException() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("guest");
        when(usersRepository.findByLogin("guest")).thenReturn(guestUser);

        assertThrows(RuntimeException.class, () -> advertisementService.addAdvertisement(adDto));
    }

    @Test
    void deleteAdvertisement_Admin_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(usersRepository.findByLogin("admin")).thenReturn(adminUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        advertisementService.deleteAdvertisement(1);

        verify(adsRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteAdvertisement_Owner_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(usersRepository.findByLogin("user1")).thenReturn(regularUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        advertisementService.deleteAdvertisement(1);

        verify(adsRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteAdvertisement_NotOwnerNotAdmin_ThrowsException() {
        Users otherUser = new Users();
        otherUser.setId(4);
        otherUser.setLogin("other");
        otherUser.setRole("Пользователь");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("other");
        when(usersRepository.findByLogin("other")).thenReturn(otherUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        assertThrows(RuntimeException.class, () -> advertisementService.deleteAdvertisement(1));
    }

    @Test
    void updateAdvertisement_Owner_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(usersRepository.findByLogin("user1")).thenReturn(regularUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));
        when(categoriesRepository.findById(1)).thenReturn(Optional.of(category));
        when(adsRepository.save(any(Ads.class))).thenReturn(ad);

        adDto.setTitle("Обновленный телефон");
        Ads result = advertisementService.updateAdvertisement(1, adDto);

        assertNotNull(result);
        assertEquals("Обновленный телефон", result.getTitle());
        verify(adsRepository, times(1)).save(any(Ads.class));
    }

    @Test
    void getAdvertisementById_Success() {
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        Ads result = advertisementService.getAdvertisementById(1);

        assertNotNull(result);
        assertEquals("Телефон", result.getTitle());
    }

    @Test
    void getAdvertisementById_NotFound_ThrowsException() {
        when(adsRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> advertisementService.getAdvertisementById(1));
    }

    @Test
    void getAdvertisementsByCategoryTitle_Success() {
        List<Ads> adsList = Collections.singletonList(ad);
        when(adsRepository.findByIdCategory_Title("Электроника")).thenReturn(adsList);

        List<Ads> result = advertisementService.getAdvertisementsByCategoryTitle("Электроника");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Телефон", result.get(0).getTitle());
    }

    @Test
    void getAllAdvertisements_Success() {
        List<Ads> adsList = Collections.singletonList(ad);
        when(adsRepository.findAll()).thenReturn(adsList);

        List<Ads> result = advertisementService.getAllAdvertisements();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Телефон", result.get(0).getTitle());
    }

    @Test
    void isAdOwner_True() {
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        boolean result = advertisementService.isAdOwner(1, 1);

        assertTrue(result);
    }

    @Test
    void isAdOwner_False() {
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        boolean result = advertisementService.isAdOwner(1, 2);

        assertFalse(result);
    }

    @Test
    void compressImage_Success() throws IOException {
        // Создаем тестовое изображение
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));

        byte[] compressedImage = advertisementService.compressImage(multipartFile, 0.5f);

        assertNotNull(compressedImage);
        assertTrue(compressedImage.length > 0);
    }

    @Test
    void compressImage_IOException_ThrowsException() throws IOException {
        when(multipartFile.getInputStream()).thenThrow(new IOException("Test exception"));

        assertThrows(IOException.class, () -> advertisementService.compressImage(multipartFile, 0.5f));
    }
}