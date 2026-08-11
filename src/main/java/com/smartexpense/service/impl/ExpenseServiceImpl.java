package com.smartexpense.service.impl;

import com.smartexpense.dto.ExpenseDTO;
import com.smartexpense.entity.Category;
import com.smartexpense.entity.Expense;
import com.smartexpense.entity.PaymentMethod;
import com.smartexpense.entity.User;
import com.smartexpense.exception.ResourceNotFoundException;
import com.smartexpense.repository.CategoryRepository;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.repository.ExpenseSpecifications;
import com.smartexpense.repository.PaymentMethodRepository;
import com.smartexpense.service.ExpenseService;
import com.smartexpense.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseServiceImpl.class);

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserService userService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              CategoryRepository categoryRepository,
                              PaymentMethodRepository paymentMethodRepository,
                              UserService userService) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Expense> search(String query, Long categoryId, Long paymentMethodId,
                                LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Long ownerId = userService.getCurrentUser().getId();
        return expenseRepository.findAll(
                ExpenseSpecifications.withFilters(ownerId, query, categoryId, paymentMethodId, fromDate, toDate),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getById(Long id) {
        Long ownerId = userService.getCurrentUser().getId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));
        // Enforce ownership: users can only access their own expenses
        if (expense.getOwner() == null || !expense.getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Expense not found with id " + id);
        }
        return expense;
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO getDtoById(Long id) {
        Expense e = getById(id);
        return ExpenseDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .amount(e.getAmount())
                .expenseDate(e.getExpenseDate())
                .categoryId(e.getCategory().getId())
                .paymentMethodId(e.getPaymentMethod().getId())
                .merchantName(e.getMerchantName())
                .description(e.getDescription())
                .build();
    }

    @Override
    public Expense create(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setOwner(userService.getCurrentUser());
        applyDto(expense, dto);
        Expense saved = expenseRepository.save(expense);
        log.info("Created expense id={} title={} amount={} owner={}",
                saved.getId(), saved.getTitle(), saved.getAmount(), saved.getOwner().getUsername());
        return saved;
    }

    @Override
    public Expense update(Long id, ExpenseDTO dto) {
        Expense expense = getById(id);
        applyDto(expense, dto);
        Expense saved = expenseRepository.save(expense);
        log.info("Updated expense id={} title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Override
    public void delete(Long id) {
        Expense expense = getById(id);
        expenseRepository.delete(expense);
        log.info("Deleted expense id={} title={}", id, expense.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    private void applyDto(Expense expense, ExpenseDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + dto.getCategoryId()));
        PaymentMethod paymentMethod = paymentMethodRepository.findById(dto.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id " + dto.getPaymentMethodId()));

        expense.setTitle(dto.getTitle().trim());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        expense.setMerchantName(dto.getMerchantName());
        expense.setDescription(dto.getDescription());
    }
}
