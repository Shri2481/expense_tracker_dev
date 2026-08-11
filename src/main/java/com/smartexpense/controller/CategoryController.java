package com.smartexpense.controller;

import com.smartexpense.dto.CategoryDTO;
import com.smartexpense.entity.Category;
import com.smartexpense.exception.BusinessException;
import com.smartexpense.exception.DuplicateResourceException;
import com.smartexpense.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        prepareModel(model);
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryDTO());
        }
        return "category/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("category") CategoryDTO category,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            prepareModel(model);
            model.addAttribute("openForm", true);
            return "category/list";
        }
        try {
            categoryService.create(category);
            ra.addFlashAttribute("success", "Category created successfully");
        } catch (DuplicateResourceException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category c = categoryService.getById(id);
        model.addAttribute("category", CategoryDTO.builder()
                .id(c.getId()).name(c.getName()).description(c.getDescription()).build());
        prepareModel(model);
        model.addAttribute("openForm", true);
        return "category/list";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("category") CategoryDTO category,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            prepareModel(model);
            model.addAttribute("openForm", true);
            return "category/list";
        }
        try {
            categoryService.update(id, category);
            ra.addFlashAttribute("success", "Category updated successfully");
        } catch (DuplicateResourceException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Category deleted successfully");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/categories";
    }

    private void prepareModel(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("activePage", "categories");
    }
}
