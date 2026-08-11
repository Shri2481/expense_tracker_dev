package com.smartexpense.service.impl;

import com.smartexpense.dto.CategoryDTO;
import com.smartexpense.entity.Category;
import com.smartexpense.exception.BusinessException;
import com.smartexpense.exception.DuplicateResourceException;
import com.smartexpense.exception.ResourceNotFoundException;
import com.smartexpense.repository.CategoryRepository;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    @Override
    public Category create(CategoryDTO dto) {
        String name = dto.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A category named '" + name + "' already exists");
        }
        Category category = Category.builder()
                .name(name)
                .description(dto.getDescription())
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Created category id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public Category update(Long id, CategoryDTO dto) {
        Category category = getById(id);
        String name = dto.getName().trim();
        categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("A category named '" + name + "' already exists");
            }
        });
        category.setName(name);
        category.setDescription(dto.getDescription());
        Category saved = categoryRepository.save(category);
        log.info("Updated category id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void delete(Long id) {
        Category category = getById(id);
        long usage = expenseRepository.countByCategoryId(id);
        if (usage > 0) {
            throw new BusinessException("Cannot delete category '" + category.getName()
                    + "' because it is used by " + usage + " expense(s)");
        }
        categoryRepository.delete(category);
        log.info("Deleted category id={} name={}", id, category.getName());
    }
}
