package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {

    // Все категории, у которых parentCategory = null (корневые категории)
    List<Categories> findByParentCategoryIsNull();

    // Все подкатегории для определенной родительской категории
    List<Categories> findByParentCategoryId(Integer parentCategoryId);
}