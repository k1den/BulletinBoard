package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.entity.Categories;
import com.example.electronic_bulletin_board.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Категории", description = "API для управления категориями")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Создать новую категорию", description = "Создает новую категорию с указанным названием и родительской категорией (если есть)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<?> createCategory(
            @Parameter(description = "Название категории", example = "Электроника", required = true)
            @RequestParam String title,
            @Parameter(description = "ID родительской категории (опционально)", example = "1")
            @RequestParam(required = false) Integer parentCategoryId) {
        try {
            Categories category = categoryService.createCategory(title, parentCategoryId);
            return new ResponseEntity<>(category, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/root")
    @Operation(summary = "Получить все корневые категории", description = "Возвращает список всех категорий, у которых нет родительской категории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список корневых категорий успешно получен")
    })
    public ResponseEntity<List<Categories>> getAllRootCategories() {
        List<Categories> categories = categoryService.getAllRootCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/subcategories/{parentCategoryId}")
    @Operation(summary = "Получить подкатегории", description = "Возвращает список всех подкатегорий для указанной родительской категории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подкатегорий успешно получен"),
            @ApiResponse(responseCode = "404", description = "Родительская категория не найдена")
    })
    public ResponseEntity<List<Categories>> getSubcategories(
            @Parameter(description = "ID родительской категории", example = "1", required = true)
            @PathVariable Integer parentCategoryId) {
        List<Categories> subcategories = categoryService.getSubcategories(parentCategoryId);
        return new ResponseEntity<>(subcategories, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить категорию по ID", description = "Возвращает категорию по её уникальному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<Categories> getCategoryById(
            @Parameter(description = "ID категории", example = "1", required = true)
            @PathVariable Integer id) {
        Optional<Categories> category = categoryService.getCategoryById(id);
        return category.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить категорию", description = "Удаляет категорию по её уникальному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID категории", example = "1", required = true)
            @PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}