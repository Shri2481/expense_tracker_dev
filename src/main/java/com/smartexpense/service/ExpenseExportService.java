package com.smartexpense.service;

public interface ExpenseExportService {

    /**
     * Generates an .xlsx report for the given month and year.
     *
     * @param month 1-12
     * @param year  four digit year
     * @return the generated Excel file as a byte array
     */
    byte[] exportMonthly(int month, int year);
}
