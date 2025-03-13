package com.fsoft.fintern.services;

import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.UserRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {
    private final UserRepository userRepository;

    public ExcelExportService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public byte[] exportAdminUsers() throws IOException {
        List<User> interns = userRepository.findAll()
                .stream()
                .filter(user -> "ADMIN".equals(user.getRole().name()))
                .collect(Collectors.toList());

        return generateExcel(interns);
    }

    public byte[] exportGuestUsers() throws IOException {
        List<User> interns = userRepository.findAll()
                .stream()
                .filter(user -> "GUEST".equals(user.getRole().name()))
                .collect(Collectors.toList());

        return generateExcel(interns);
    }

    public byte[] exportEmployeeUsers() throws IOException {
        List<User> interns = userRepository.findAll()
                .stream()
                .filter(user -> "EMPLOYEE".equals(user.getRole().name()))
                .collect(Collectors.toList());

        return generateExcel(interns);
    }

    public byte[] exportInternUsers() throws IOException {
        List<User> interns = userRepository.findAll()
                .stream()
                .filter(user -> "INTERN".equals(user.getRole().name()))
                .collect(Collectors.toList());

        return generateExcel(interns);
    }


    private byte[] generateExcel(List<User> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            boolean hasIntern = users.stream().anyMatch(user -> "INTERN".equals(user.getRole().name()));

            String[] internHeaders = {"First Name", "Last Name", "Email", "Phone Number", "Class ID", "Gender", "Role"};
            String[] Headers = {"First Name", "Last Name", "Email", "Phone Number", "Gender", "Role"};
            String[] headers = hasIntern ? internHeaders : Headers;

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getFirst_name());
                row.createCell(1).setCellValue(user.getLast_name());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getPhone_number());

                if ("INTERN".equals(user.getRole().name())) {
                    row.createCell(4).setCellValue(user.getClassroom() != null ? user.getClassroom().getClassName() : "N/A");
                    row.createCell(5).setCellValue(user.getGender().name());
                    row.createCell(6).setCellValue(user.getRole().name());
                } else {
                    row.createCell(4).setCellValue(user.getGender().name());
                    row.createCell(5).setCellValue(user.getRole().name());
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
