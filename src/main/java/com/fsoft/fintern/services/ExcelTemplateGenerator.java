package com.fsoft.fintern.services;

import com.fsoft.fintern.enums.ClassType;
import com.fsoft.fintern.enums.LanguageType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@Service
public class ExcelTemplateGenerator {

    public byte[] generateSchedulingTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create a sheet for classes
            createClassesSheet(workbook);

            // Create a sheet for mentors
            createMentorsSheet(workbook);

            // Create a sheet for time slots
            createTimeSlotsSheet(workbook);

            // Create a sheet for rooms
            createRoomsSheet(workbook);

            // Create a config sheet
            createConfigSheet(workbook);

            // Write the workbook to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createClassesSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Classes");
        Row headerRow = sheet.createRow(0);

        // Create header cells
        Cell cellClassId = headerRow.createCell(0);
        cellClassId.setCellValue("ClassID");

        Cell cellClassName = headerRow.createCell(1);
        cellClassName.setCellValue("ClassName");

        Cell cellClassType = headerRow.createCell(2);
        cellClassType.setCellValue("ClassType");

        Cell cellLanguageType = headerRow.createCell(3);
        cellLanguageType.setCellValue("LanguageType");

        Cell cellRoomId = headerRow.createCell(4);
        cellRoomId.setCellValue("RoomID (Optional)");

        // Add dropdown list for ClassType
        createDropdownList(workbook, sheet, 1, 100, 2, 2,
                Arrays.stream(ClassType.values()).map(Enum::name).toArray(String[]::new));

        // Add dropdown list for LanguageType
        createDropdownList(workbook, sheet, 1, 100, 3, 3,
                Arrays.stream(LanguageType.values()).map(Enum::name).toArray(String[]::new));

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMentorsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Mentors");
        Row headerRow = sheet.createRow(0);

        // Create header cells
        Cell cellMentorId = headerRow.createCell(0);
        cellMentorId.setCellValue("MentorID");

        Cell cellName = headerRow.createCell(1);
        cellName.setCellValue("Name");

        Cell cellSpecialization = headerRow.createCell(2);
        cellSpecialization.setCellValue("Specialization");

        Cell cellMaxHoursPerWeek = headerRow.createCell(3);
        cellMaxHoursPerWeek.setCellValue("MaxHoursPerWeek");

        // Add dropdown list for Specialization
        createDropdownList(workbook, sheet, 1, 100, 2, 2,
                new String[]{"CODE", "JAPANESE", "KOREAN"});

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTimeSlotsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("TimeSlots");
        Row headerRow = sheet.createRow(0);

        // Create header cells
        Cell cellSlotId = headerRow.createCell(0);
        cellSlotId.setCellValue("SlotID");

        Cell cellDayOfWeek = headerRow.createCell(1);
        cellDayOfWeek.setCellValue("DayOfWeek");

        Cell cellStartTime = headerRow.createCell(2);
        cellStartTime.setCellValue("StartTime");

        Cell cellEndTime = headerRow.createCell(3);
        cellEndTime.setCellValue("EndTime");

        Cell cellSlotType = headerRow.createCell(4);
        cellSlotType.setCellValue("SlotType");

        // Add dropdown list for DayOfWeek
        createDropdownList(workbook, sheet, 1, 100, 1, 1,
                new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"});

        // Add dropdown list for SlotType
        createDropdownList(workbook, sheet, 1, 100, 4, 4,
                new String[]{"LANG_SHORT", "LANG_LONG", "CODE_AM", "CODE_PM"});

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createRoomsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Rooms");
        Row headerRow = sheet.createRow(0);

        // Create header cells
        Cell cellRoomId = headerRow.createCell(0);
        cellRoomId.setCellValue("RoomID");

        Cell cellRoomName = headerRow.createCell(1);
        cellRoomName.setCellValue("RoomName");

        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createConfigSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Config");

        // Create rows for each configuration
        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("MaxClassesPerRoom");
        row0.createCell(1).setCellValue(5);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("PriorityForCombinedClass");
        row1.createCell(1).setCellValue(3);

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("PriorityForLanguageClass");
        row2.createCell(1).setCellValue(2);

        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue("PriorityForCodeClass");
        row3.createCell(1).setCellValue(1);

        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDropdownList(Workbook workbook, Sheet sheet, int firstRow, int lastRow,
                                    int firstCol, int lastCol, String[] values) {
        // Kiểm tra phạm vi ô hợp lệ
        if (firstRow > lastRow || firstCol > lastCol) {
            throw new IllegalArgumentException("Invalid cell range: firstRow=" + firstRow + ", lastRow=" + lastRow +
                    ", firstCol=" + firstCol + ", lastCol=" + lastCol);
        }

        // Create a hidden sheet for the list values
        String hiddenSheetName = "Hidden_" + sheet.getSheetName() + "_" + firstCol + "_" + lastCol;
        Sheet hiddenSheet = workbook.createSheet(hiddenSheetName);
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheetName), true);

        // Populate the hidden sheet with the list values
        for (int i = 0; i < values.length; i++) {
            Row row = hiddenSheet.createRow(i);
            row.createCell(0).setCellValue(values[i]);
        }

        // Define the name range that refers to the list
        String rangeName = "Range_" + sheet.getSheetName() + "_" + firstCol + "_" + lastCol;
        Name name = workbook.createName();
        name.setNameName(rangeName);
        name.setRefersToFormula(hiddenSheetName + "!$A$1:$A$" + values.length);

        // Create the data validation for the dropdown
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("=" + rangeName);
        DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
}