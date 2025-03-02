package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.model.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {

    // Находим все категории, у которых parentCategory = null (корневые категории)
    List<Categories> findByParentCategoryIsNull();

    // Находим все подкатегории для определенной родительской категории
    List<Categories> findByParentCategoryId(Integer parentCategoryId);
}