package com.smartexpense.service.impl;

import com.smartexpense.dto.DashboardDTO;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.service.DashboardService;
import com.smartexpense.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public DashboardServiceImpl(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Override
    public DashboardDTO getDashboard() {
        Long ownerId = userService.getCurrentUser().getId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        return DashboardDTO.builder()
                .totalExpenses(expenseRepository.sumAllByOwner(ownerId))
                .todayExpenses(expenseRepository.sumByOwnerAndDate(ownerId, today))
                .currentMonthExpenses(expenseRepository.sumByOwnerBetween(ownerId, monthStart, monthEnd))
                .totalTransactions(expenseRepository.countByOwnerId(ownerId))
                .recentExpenses(expenseRepository.findTop5ByOwnerIdOrderByExpenseDateDescIdDesc(ownerId))
                .categorySummary(expenseRepository.getCategorySummaryByOwner(ownerId))
                .build();
    }
}
