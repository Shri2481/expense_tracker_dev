package com.smartexpense.service;

import com.smartexpense.dto.CategoryDTO;
import com.smartexpense.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAll();

    Category getById(Long id);

    Category create(CategoryDTO dto);

    Category update(Long id, CategoryDTO dto);

    void delete(Long id);
}
