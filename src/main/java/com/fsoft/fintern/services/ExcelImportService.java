package com.fsoft.fintern.services;

import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.UserRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelImportService {
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public ExcelImportService(UserRepository userRepository, ClassroomRepository classroomRepository) {
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public int importUsers(MultipartFile file) throws IOException {
        List<User> users = readExcel(file);
        List<User> newUsers = new ArrayList<>();

        for (User user : users) {
            if (!userRepository.existsByEmail(user.getEmail())) { 
                newUsers.add(user);
            }
        }

        if (!newUsers.isEmpty()) {
            userRepository.saveAll(newUsers);
        }

        return newUsers.size();
    }

    private List<User> readExcel(MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                User user = new User();

                String email = getCellValue(row.getCell(2));
                boolean exists = userRepository.existsByEmail(email);
                if (exists) { continue; }

                user.setFirst_name(getCellValue(row.getCell(0)));
                user.setLast_name(getCellValue(row.getCell(1)));
                user.setEmail(email);
                user.setPhone_number(getCellValue(row.getCell(3)));
                Role role = Role.valueOf(getCellValue(row.getCell(5)));
                user.setRole(role);

                /* will fix later

                if(role == Role.GUEST) {
                    role = Role.INTERN;
                }

                 */

                if (role == Role.INTERN) {
                    String className = getCellValue(row.getCell(4));
                    Optional<Classroom> classroomOpt = classroomRepository.findClassroomByClassName(className);
                    if (classroomOpt.isPresent()) {
                        user.setClassroom(classroomOpt.get());
                    } else {
                        throw new RuntimeException(className + " not available!");
                    }
                    user.setGender(Gender.valueOf(getCellValue(row.getCell(5))));
                } else {
                    user.setGender(Gender.valueOf(getCellValue(row.getCell(4))));
                }

                users.add(user);
            }
        }

        return users;
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}

