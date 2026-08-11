package com.smartexpense.controller;

import com.smartexpense.dto.ExpenseDTO;
import com.smartexpense.entity.Expense;
import com.smartexpense.service.CategoryService;
import com.smartexpense.service.ExpenseExportService;
import com.smartexpense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private static final List<Integer> PAGE_SIZES = List.of(10, 25, 50);

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final ExpenseExportService exportService;

    public ExpenseController(ExpenseService expenseService,
                             CategoryService categoryService,
                             ExpenseExportService exportService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.exportService = exportService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String query,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long paymentMethodId,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        if (!PAGE_SIZES.contains(size)) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), size,
                Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<Expense> expensePage = expenseService.search(query, categoryId, paymentMethodId, fromDate, toDate, pageable);

        model.addAttribute("expensePage", expensePage);
        model.addAttribute("expenses", expensePage.getContent());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("paymentMethods", expenseService.getAllPaymentMethods());
        model.addAttribute("query", query);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("paymentMethodId", paymentMethodId);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("size", size);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("months", months());
        model.addAttribute("currentYear", LocalDate.now().getYear());
        model.addAttribute("currentMonth", LocalDate.now().getMonthValue());
        model.addAttribute("activePage", "expenses");
        return "expense/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        if (!model.containsAttribute("expense")) {
            model.addAttribute("expense", ExpenseDTO.builder().expenseDate(LocalDate.now()).build());
        }
        prepareFormModel(model);
        return "expense/add";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("expense") ExpenseDTO expense,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            prepareFormModel(model);
            return "expense/add";
        }
        expenseService.create(expense);
        ra.addFlashAttribute("success", "Expense created successfully");
        return "redirect:/expenses";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("expense", expenseService.getById(id));
        model.addAttribute("activePage", "expenses");
        return "expense/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("expense")) {
            model.addAttribute("expense", expenseService.getDtoById(id));
        }
        model.addAttribute("expenseId", id);
        prepareFormModel(model);
        return "expense/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("expense") ExpenseDTO expense,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("expenseId", id);
            prepareFormModel(model);
            return "expense/edit";
        }
        expenseService.update(id, expense);
        ra.addFlashAttribute("success", "Expense updated successfully");
        return "redirect:/expenses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        expenseService.delete(id);
        ra.addFlashAttribute("success", "Expense deleted successfully");
        return "redirect:/expenses";
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam int month, @RequestParam int year) {
        byte[] data = exportService.exportMonthly(month, year);
        String monthName = LocalDate.of(year, month, 1)
                .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String filename = "expenses_" + monthName + "_" + year + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(org.springframework.http.ContentDisposition
                .attachment().filename(filename).build());
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, org.springframework.http.HttpStatus.OK);
    }

    private void prepareFormModel(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("paymentMethods", expenseService.getAllPaymentMethods());
        model.addAttribute("activePage", "expenses");
    }

    private List<Month> months() {
        return java.util.Arrays.stream(java.time.Month.values())
                .map(m -> new Month(m.getValue(), m.getDisplayName(TextStyle.FULL, Locale.ENGLISH)))
                .toList();
    }

    public record Month(int value, String name) {
    }
}
