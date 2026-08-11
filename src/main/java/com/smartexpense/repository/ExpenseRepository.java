package com.smartexpense.repository;

import com.smartexpense.dto.CategorySummaryDTO;
import com.smartexpense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    List<Expense> findTop5ByOwnerIdOrderByExpenseDateDescIdDesc(Long ownerId);

    List<Expense> findByOwnerIdAndExpenseDateBetweenOrderByExpenseDateAscIdAsc(Long ownerId, LocalDate start, LocalDate end);

    long countByCategoryId(Long categoryId);

    long countByOwnerId(Long ownerId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.owner.id = :ownerId")
    BigDecimal sumAllByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.owner.id = :ownerId AND e.expenseDate = :date")
    BigDecimal sumByOwnerAndDate(@Param("ownerId") Long ownerId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.owner.id = :ownerId AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByOwnerBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT new com.smartexpense.dto.CategorySummaryDTO(e.category.name, SUM(e.amount)) " +
            "FROM Expense e WHERE e.owner.id = :ownerId GROUP BY e.category.name ORDER BY SUM(e.amount) DESC")
    List<CategorySummaryDTO> getCategorySummaryByOwner(@Param("ownerId") Long ownerId);
}
