package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import com.fsoft.fintern.repositories.CVInfoRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.*;

@Service
public class OCR_Service {

    @Autowired
    private CVInfoRepository cvInfoRepository;

    public Map<String, List<String>> processGoogleDriveLink(String driveLink) throws IOException, InterruptedException, TesseractException {
        Tesseract tesseract = new Tesseract();
        String text = "";
        File tempFile = null;
        try {
            tesseract.setDatapath("src/main/resources/tessdata");
            tesseract.setOcrEngineMode(1);
            tesseract.setPageSegMode(3);

            tempFile = downloadToTempFile(driveLink);

            if (!isDownloadSuccessful(tempFile)) {
                throw new IOException("Download failed. Please ensure the Google Drive link is public and accessible.");
            }

            text = tesseract.doOCR(tempFile);
            System.out.println("Raw OCR Output:\n" + text); // Debug văn bản thô
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        text = cleanText(text);
        return extractCVInfo(text);
    }

    public void saveToDatabase(Map<String, List<String>> cvData, int fileId, int recruitmentId) {
        CVInfo cvInfo = new CVInfo();

        // Sử dụng fileId và recruitmentId từ controller thay vì hardcode
        CVInfoId cvInfoId = new CVInfoId(recruitmentId, fileId);
        cvInfo.setId(cvInfoId);

        // Lấy GPA và chuyển đổi
        if (!cvData.get("GPA").isEmpty()) {
            String gpaStr = cvData.get("GPA").get(0); // "8.0/10" hoặc "3.2/4"
            try {
                String[] gpaParts = gpaStr.split("/");
                Double gpaValue = Double.parseDouble(gpaParts[0]);
                Double gpaScale = Double.parseDouble(gpaParts[1]);

                if (gpaScale == 10.0) {

                    gpaValue = (gpaValue / 10.0) * 4.0;
                }
                cvInfo.setGpa(gpaValue);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                cvInfo.setGpa(0.0);
            }
        } else {
            cvInfo.setGpa(0.0); // Giá trị mặc định nếu không có GPA
        }

        // Lấy Education
        if (!cvData.get("Education").isEmpty()) {
            cvInfo.setEducation(cvData.get("Education").get(0));
        } else {
            cvInfo.setEducation(""); // Giá trị mặc định nếu không có Education
        }

        // Lấy Skills (chuyển list thành chuỗi, phân tách bằng dấu phẩy)
        if (!cvData.get("Skills").isEmpty()) {
            cvInfo.setSkill(String.join(", ", cvData.get("Skills")));
        } else {
            cvInfo.setSkill(""); // Giá trị mặc định nếu không có Skills
        }

        // Lưu vào database
        cvInfoRepository.save(cvInfo);
    }

    private File downloadToTempFile(String driveLink) throws IOException, InterruptedException {
        String fileId = extractFileIdFromLink(driveLink);
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        System.out.println("Initial Download URL: " + downloadUrl);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .build();

        HttpResponse<String> initialResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = initialResponse.body();

        Pattern confirmPattern = Pattern.compile("confirm=([a-zA-Z0-9_-]+)");
        Matcher confirmMatcher = confirmPattern.matcher(responseBody);
        if (confirmMatcher.find()) {
            String confirmCode = confirmMatcher.group(1);
            downloadUrl = downloadUrl + "&confirm=" + confirmCode;
            System.out.println("Bypass URL: " + downloadUrl);
            request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .build();
        }

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        File tempFile = File.createTempFile("ocr-", ".pdf");

        try (InputStream in = response.body();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    private boolean isDownloadSuccessful(File tempFile) throws IOException {
        if (tempFile.length() == 0) {
            System.out.println("File size is 0 bytes - Download failed");
            return false;
        }

        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] header = new byte[5];
            int bytesRead = fis.read(header);
            if (bytesRead < 4) {
                System.out.println("File too small to be a valid PDF");
                return false;
            }
            String headerStr = new String(header).trim();
            System.out.println("File header (raw): " + headerStr);
            if (!headerStr.startsWith("%PDF")) {
                System.out.println("File is not a valid PDF");
                System.out.println("File content preview:");
                try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                    String line;
                    int linesPrinted = 0;
                    while ((line = reader.readLine()) != null && linesPrinted < 10) {
                        System.out.println(line);
                        linesPrinted++;
                    }
                }
                return false;
            }
        }

        return true;
    }

    private String extractFileIdFromLink(String driveLink) {
        Pattern pattern = Pattern.compile("file/d/([a-zA-Z0-9_-]+)/");
        Matcher matcher = pattern.matcher(driveLink);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid Google Drive link: " + driveLink);
    }

    private String cleanText(String text) {
        return text.replaceAll("[\\uD800-\\uDFFF]", "");
    }

    private String normalizeGPA(String gpaStr) {
        if (gpaStr != null) {
            return gpaStr.replace("'", ".").replace(",", ".");
        }
        return null;
    }

    private Map<String, List<String>> extractCVInfo(String text) {
        Map<String, List<String>> cvData = new LinkedHashMap<>();
        cvData.put("GPA", new ArrayList<>());
        cvData.put("Education", new ArrayList<>());
        cvData.put("Skills", new ArrayList<>());

        // Trích xuất GPA
        Pattern gpaPattern = Pattern.compile("GPA:?\\s*([\\d]+[,'\\.]\\d+/[\\d]+)", Pattern.CASE_INSENSITIVE);
        Matcher gpaMatcher = gpaPattern.matcher(text);
        if (gpaMatcher.find()) {
            String gpa = normalizeGPA(gpaMatcher.group(1));
            cvData.get("GPA").add(gpa);
        }

        // Trích xuất Education (chỉ lấy FPT University)
        Pattern educationPattern = Pattern.compile(
                "(?i)FPT\\s*UNIVERSITY",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        Matcher educationMatcher = educationPattern.matcher(text);
        while (educationMatcher.find()) {
            String education = educationMatcher.group().trim();
            cvData.get("Education").add(education);
        }

        // Trích xuất Skills
        String[] programmingLanguages = {"Java", "HTML", "CSS", "JavaScript", "SQL", "C", "C\\+", "C\\+\\+", "Python", "SQLServer"};
        String skillsPatternStr = "\\b(" + String.join("|", programmingLanguages) + ")\\b";
        Pattern skillsPattern = Pattern.compile(skillsPatternStr, Pattern.CASE_INSENSITIVE);
        Matcher skillsMatcher = skillsPattern.matcher(text);
        Set<String> skillsSet = new HashSet<>();
        while (skillsMatcher.find()) {
            skillsSet.add(skillsMatcher.group());
        }
        cvData.get("Skills").addAll(skillsSet);

        return cvData;
    }
}