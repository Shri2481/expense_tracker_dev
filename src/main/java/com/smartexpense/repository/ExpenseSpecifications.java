package com.smartexpense.repository;

import com.smartexpense.entity.Expense;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> withFilters(Long ownerId, String query, Long categoryId, Long paymentMethodId,
                                                     LocalDate fromDate, LocalDate toDate) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always scope results to the owner (current logged-in user)
            predicates.add(cb.equal(root.get("owner").get("id"), ownerId));

            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("merchantName")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (paymentMethodId != null) {
                predicates.add(cb.equal(root.get("paymentMethod").get("id"), paymentMethodId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
