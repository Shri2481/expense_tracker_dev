package com.smartexpense.service.impl;

import com.smartexpense.entity.Expense;
import com.smartexpense.exception.BusinessException;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.service.ExpenseExportService;
import com.smartexpense.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class ExpenseExportServiceImpl implements ExpenseExportService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseExportServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] HEADERS = {
            "Date", "Title", "Amount", "Category", "Payment Method", "Merchant Name", "Description"
    };

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpenseExportServiceImpl(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Override
    public byte[] exportMonthly(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BusinessException("Invalid month: " + month + ". Month must be between 1 and 12.");
        }
        if (year < 1970 || year > 9999) {
            throw new BusinessException("Invalid year: " + year);
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        String monthName = start.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<Expense> expenses = expenseRepository.findByOwnerIdAndExpenseDateBetweenOrderByExpenseDateAscIdAsc(
                userService.getCurrentUser().getId(), start, end);
        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Generating Excel report for {} {} with {} transactions", monthName, year, expenses.size());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expenses");

            CellStyle titleStyle = titleStyle(workbook);
            CellStyle summaryStyle = summaryStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle bodyStyle = bodyStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook, bodyStyle);
            CellStyle amountStyle = amountStyle(workbook, bodyStyle);

            int rowIdx = 0;

            // ----- Summary block -----
            Row titleRow = sheet.createRow(rowIdx++);
            createCell(titleRow, 0, "Expense Report", titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 6));

            Row monthRow = sheet.createRow(rowIdx++);
            createCell(monthRow, 0, "Month: " + monthName + " " + year, summaryStyle);
            sheet.addMergedRegion(new CellRangeAddress(monthRow.getRowNum(), monthRow.getRowNum(), 0, 6));

            Row totalRow = sheet.createRow(rowIdx++);
            createCell(totalRow, 0, "Total Expenses: \u20B9 " + total.toPlainString(), summaryStyle);
            sheet.addMergedRegion(new CellRangeAddress(totalRow.getRowNum(), totalRow.getRowNum(), 0, 6));

            Row countRow = sheet.createRow(rowIdx++);
            createCell(countRow, 0, "Total Transactions: " + expenses.size(), summaryStyle);
            sheet.addMergedRegion(new CellRangeAddress(countRow.getRowNum(), countRow.getRowNum(), 0, 6));

            rowIdx++; // blank spacer row

            // ----- Table header -----
            int headerRowNum = rowIdx;
            Row headerRow = sheet.createRow(rowIdx++);
            for (int c = 0; c < HEADERS.length; c++) {
                createCell(headerRow, c, HEADERS[c], headerStyle);
            }

            // ----- Data rows -----
            for (Expense e : expenses) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, e.getExpenseDate().format(DATE_FMT), dateStyle);
                createCell(row, 1, e.getTitle(), bodyStyle);

                Cell amountCell = row.createCell(2);
                amountCell.setCellValue(e.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);

                createCell(row, 3, e.getCategory().getName(), bodyStyle);
                createCell(row, 4, e.getPaymentMethod().getName(), bodyStyle);
                createCell(row, 5, e.getMerchantName() == null ? "" : e.getMerchantName(), bodyStyle);
                createCell(row, 6, e.getDescription() == null ? "" : e.getDescription(), bodyStyle);
            }

            // Column widths
            sheet.setColumnWidth(0, 3200);
            sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 4500);
            sheet.setColumnWidth(4, 4500);
            sheet.setColumnWidth(5, 6000);
            sheet.setColumnWidth(6, 9000);

            // Freeze header row (rows above and including the header stay visible)
            sheet.createFreezePane(0, headerRowNum + 1);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new BusinessException("Failed to generate Excel report: " + ex.getMessage());
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle titleStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle summaryStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle headerStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        applyBorders(style);
        return style;
    }

    private CellStyle bodyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorders(style);
        return style;
    }

    private CellStyle dateStyle(Workbook wb, CellStyle base) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle amountStyle(Workbook wb, CellStyle base) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("\u20B9 #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void applyBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
