package com.smartexpense.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDTO {

    private String categoryName;
    private BigDecimal total;
}
