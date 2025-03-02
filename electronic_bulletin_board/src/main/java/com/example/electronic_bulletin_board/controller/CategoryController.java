package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.model.Categories;
import com.example.electronic_bulletin_board.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Создание новой категории
    @PostMapping
    public ResponseEntity<Categories> createCategory(
            @RequestParam String title,
            @RequestParam(required = false) Integer parentCategoryId) {
        Categories category = categoryService.createCategory(title, parentCategoryId);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    // Получение всех корневых категорий
    @GetMapping("/root")
    public ResponseEntity<List<Categories>> getAllRootCategories() {
        List<Categories> categories = categoryService.getAllRootCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Получение всех подкатегорий для определенной родительской категории
    @GetMapping("/subcategories/{parentCategoryId}")
    public ResponseEntity<List<Categories>> getSubcategories(
            @PathVariable Integer parentCategoryId) {
        List<Categories> subcategories = categoryService.getSubcategories(parentCategoryId);
        return new ResponseEntity<>(subcategories, HttpStatus.OK);
    }

    // Получение категории по ID
    @GetMapping("/{id}")
    public ResponseEntity<Categories> getCategoryById(
            @PathVariable Integer id) {
        Optional<Categories> category = categoryService.getCategoryById(id);
        return category.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Удаление категории
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}