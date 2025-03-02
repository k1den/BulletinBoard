package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.model.Categories;
import com.example.electronic_bulletin_board.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Создание новой категории
    public Categories createCategory(String title, Integer parentCategoryId) {
        Categories category = new Categories();
        category.setTitle(title);

        if (parentCategoryId != null) {
            Categories parentCategory = categoryRepository.findById(parentCategoryId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParentCategory(parentCategory);
        }

        return categoryRepository.save(category);
    }

    // Получение всех корневых категорий
    public List<Categories> getAllRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    // Получение всех подкатегорий для определенной родительской категории
    public List<Categories> getSubcategories(Integer parentCategoryId) {
        return categoryRepository.findByParentCategoryId(parentCategoryId);
    }

    // Получение категории по ID
    public Optional<Categories> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    // Удаление категории
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}