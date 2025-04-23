package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.dto.AdvertisementDto;
import com.example.electronic_bulletin_board.entity.Ads;
import com.example.electronic_bulletin_board.entity.Categories;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.CategoryRepository;
import com.example.electronic_bulletin_board.service.AdvertisementService;
import com.example.electronic_bulletin_board.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
@Tag(name = "API для управления объявлениями", description = "Предоставляет методы для работы с объявлениями")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    @Operation(summary = "Добавление нового объявления", description = "Создает новое объявление с заданными параметрами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление успешно добавлено"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<?> addAdvertisement(
            @RequestParam String title,
            @RequestParam Integer idCategory,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) MultipartFile photo) {
        try {
            byte[] photoBytes = (photo != null && !photo.isEmpty()) ? AdvertisementService.compressImage(photo, 0.5f) : null;

            AdvertisementDto advertisementDto = new AdvertisementDto();
            advertisementDto.setTitle(title);
            advertisementDto.setIdCategory(idCategory);
            advertisementDto.setDescription(description);
            advertisementDto.setPrice(price);
            advertisementDto.setPhoto(photoBytes);

            Ads ads = advertisementService.addAdvertisement(advertisementDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ads);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление объявления", description = "Удаляет объявление по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление успешно удалено"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<?> deleteAdvertisement(@PathVariable Integer id) {
        try {
            advertisementService.deleteAdvertisement(id);
            return ResponseEntity.ok("Объявление успешно удалено");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdvertisement(
            @PathVariable Integer id,
            @RequestParam String title,
            @RequestParam Integer idCategory,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) MultipartFile photo) { // Сделаем фото необязательным
        try {
            byte[] photoBytes = (photo != null && !photo.isEmpty()) ? AdvertisementService.compressImage(photo, 0.5f) : null;

            AdvertisementDto advertisementDto = new AdvertisementDto();
            advertisementDto.setTitle(title);
            advertisementDto.setIdCategory(idCategory);
            advertisementDto.setDescription(description);
            advertisementDto.setPrice(price);
            advertisementDto.setPhoto(photoBytes);

            Ads ads = advertisementService.updateAdvertisement(id, advertisementDto);
            return ResponseEntity.ok(ads);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
        }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Получение объявления по ID", description = "Возвращает данные объявления по его идентификатору")
    public Ads getAdvertisementById(@PathVariable Integer id) {
        return advertisementService.getAdvertisementById(id);
    }

    @GetMapping("/by-category")
    @Operation(summary = "Получение объявлений по названию категории", description = "Возвращает список объявлений, принадлежащих указанной категории")
    public List<Ads> getAdvertisementsByCategoryTitle(@RequestParam String categoryName) {
        return advertisementService.getAdvertisementsByCategoryTitle(categoryName);
    }

    @GetMapping
    @Operation(summary = "Получение всех объявлений", description = "Возвращает список всех объявлений")
    public List<Ads> getAllAdvertisements() {
        return advertisementService.getAllAdvertisements();
    }

    @GetMapping("/photo/{id}")
    @Operation(summary = "Получение фотографии объявления", description = "Возвращает фотографию объявления в формате base64")
    public ResponseEntity<String> getAdvertisementPhoto(@PathVariable Integer id) {
        Ads ad = advertisementService.getAdvertisementById(id);
        if (ad != null && ad.getPhoto() != null) {
            String base64Image = Base64.getEncoder().encodeToString(ad.getPhoto());
            return ResponseEntity.ok(base64Image);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categories")
    @Operation(summary = "Получение списка всех категорий", description = "Возвращает список всех категорий с их id и названиями")
    public List<Categories> getAllCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/current-user")
    @Operation(summary = "Получение информации о текущем пользователе", description = "Возвращает информацию о текущем пользователе")
    public ResponseEntity<Users> getCurrentUser() {
        Users currentUser = advertisementService.getCurrentUser();
        if (currentUser != null) {
            return ResponseEntity.ok(currentUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}