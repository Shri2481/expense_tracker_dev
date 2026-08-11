package com.smartexpense.dto;

import com.smartexpense.entity.Expense;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private BigDecimal totalExpenses;
    private BigDecimal todayExpenses;
    private BigDecimal currentMonthExpenses;
    private long totalTransactions;
    private List<Expense> recentExpenses;
    private List<CategorySummaryDTO> categorySummary;
}
