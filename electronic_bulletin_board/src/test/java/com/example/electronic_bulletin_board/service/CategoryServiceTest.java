package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.model.Categories;
import com.example.electronic_bulletin_board.model.Users;
import com.example.electronic_bulletin_board.repository.CategoryRepository;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    private Users mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new Users();
        mockUser.setLogin("admin");
        mockUser.setRole("Администратор");
    }

    // Тест на успешное создание категории без родительской категории
    @Test
    void testCreateCategory_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(userRepository.findByLogin("admin")).thenReturn(mockUser);
        when(categoryRepository.save(any(Categories.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categories newCategory = categoryService.createCategory("Новая категория", null);

        assertNotNull(newCategory);
        assertEquals("Новая категория", newCategory.getTitle());
        verify(categoryRepository).save(any(Categories.class));
    }

    // Тест на успешное создание подкатегории с родительской категорией
    @Test
    void testCreateCategory_WithParentCategory_Success() {
        Categories parentCategory = new Categories();
        parentCategory.setId(1);
        parentCategory.setTitle("Родительская категория");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(userRepository.findByLogin("admin")).thenReturn(mockUser);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Categories.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categories newCategory = categoryService.createCategory("Подкатегория", 1);

        assertNotNull(newCategory);
        assertEquals("Подкатегория", newCategory.getTitle());
        assertEquals(parentCategory, newCategory.getParentCategory());
        verify(categoryRepository).save(any(Categories.class));
    }

    // Тест на создание категории с несуществующей родительской категорией
    @Test
    void testCreateCategory_ParentCategoryNotFound() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(userRepository.findByLogin("admin")).thenReturn(mockUser);
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.createCategory("Новая категория", 99);
        });

        assertEquals("Родительская категория не найдена", exception.getMessage());
    }

    // Тест на получение всех корневых категорий
    @Test
    void testGetAllRootCategories() {
        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(Collections.emptyList());

        assertTrue(categoryService.getAllRootCategories().isEmpty());
        verify(categoryRepository).findByParentCategoryIsNull();
    }

    // Тест на получение подкатегорий
    @Test
    void testGetSubcategories() {
        when(categoryRepository.findByParentCategoryId(1)).thenReturn(Collections.emptyList());

        assertTrue(categoryService.getSubcategories(1).isEmpty());
        verify(categoryRepository).findByParentCategoryId(1);
    }

    // Тест на получение категории по ID
    @Test
    void testGetCategoryById_Success() {
        Categories category = new Categories();
        category.setId(1);
        category.setTitle("Категория 1");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        Optional<Categories> foundCategory = categoryService.getCategoryById(1);
        assertTrue(foundCategory.isPresent());
        assertEquals("Категория 1", foundCategory.get().getTitle());
    }

    // Тест на успешное удаление категории
    @Test
    void testDeleteCategory_Success() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(userRepository.findByLogin("admin")).thenReturn(mockUser);

        categoryService.deleteCategory(1);
        verify(categoryRepository).deleteById(1);
    }

    // Тест на попытку удаления категории пользователем без прав администратора
    @Test
    void testDeleteCategory_UserNotAdmin() {
        mockUser.setRole("Пользователь");
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("admin");
        when(userRepository.findByLogin("admin")).thenReturn(mockUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategory(1);
        });

        assertEquals("Доступ запрещен: у вас недостаточно прав", exception.getMessage());
    }

    // Тест на попытку удаления категории пользователем, не вошедшим в систему
    @Test
    void testDeleteCategory_UserNotLoggedIn() {
        when(authService.isSessionActive()).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategory(1);
        });

        assertEquals("Пользователь не вошел в систему", exception.getMessage());
    }
}
