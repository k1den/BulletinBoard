package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.dto.AdvertisementDto;
import com.example.electronic_bulletin_board.model.Ads;
import com.example.electronic_bulletin_board.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
@Tag(name = "API для управления объявлениями", description = "Предоставляет методы для работы с объявлениями")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @PostMapping
    @Operation(summary = "Добавление нового объявления", description = "Создает новое объявление с заданными параметрами")
    public ResponseEntity<?> addAdvertisement(
            @RequestParam String title,
            @RequestParam Integer idCategory,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam String photo) {
        try {
            AdvertisementDto advertisementDto = new AdvertisementDto();
            advertisementDto.setTitle(title);
            advertisementDto.setIdCategory(idCategory);
            advertisementDto.setDescription(description);
            advertisementDto.setPrice(price);
            advertisementDto.setPhoto(photo);
            Ads ads = advertisementService.addAdvertisement(advertisementDto);
            return ResponseEntity.ok(ads);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление объявления", description = "Удаляет объявление по его идентификатору")
    public ResponseEntity<?> deleteAdvertisement(@PathVariable Integer id) {
        try {
            advertisementService.deleteAdvertisement(id);
            return ResponseEntity.ok("Объявление успешно удалено");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактирование объявления", description = "Обновляет данные объявления по его идентификатору")
    public ResponseEntity<?> updateAdvertisement(
            @PathVariable Integer id,
            @RequestParam String title,
            @RequestParam Integer idCategory,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam String photo) {
        try {
            AdvertisementDto advertisementDto = new AdvertisementDto();
            advertisementDto.setTitle(title);
            advertisementDto.setIdCategory(idCategory);
            advertisementDto.setDescription(description);
            advertisementDto.setPrice(price);
            advertisementDto.setPhoto(photo);
            Ads ads = advertisementService.updateAdvertisement(id, advertisementDto);
            return ResponseEntity.ok(ads);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
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
}