package com.smartexpense.service;

import com.smartexpense.dto.ExpenseDTO;
import com.smartexpense.entity.Expense;
import com.smartexpense.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    Page<Expense> search(String query, Long categoryId, Long paymentMethodId,
                         LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Expense getById(Long id);

    ExpenseDTO getDtoById(Long id);

    Expense create(ExpenseDTO dto);

    Expense update(Long id, ExpenseDTO dto);

    void delete(Long id);

    List<PaymentMethod> getAllPaymentMethods();
}
