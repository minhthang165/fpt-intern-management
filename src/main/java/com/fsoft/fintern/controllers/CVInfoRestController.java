package com.fsoft.fintern.controllers;

import com.fsoft.fintern.services.CVInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cv-info")
public class CVInfoRestController {

    private final CVInfoService cvInfoService;

    @Autowired
    public CVInfoRestController(CVInfoService cvInfoService) {
        this.cvInfoService = cvInfoService;
    }

    /**
     * Lấy danh sách thông tin CV theo recruitmentId
     * @param recruitmentId ID của yêu cầu tuyển dụng
     * @return Danh sách CV_Info kèm thông tin người nộp
     */
    @GetMapping("/recruitment/{recruitmentId}")
    public ResponseEntity<List<Map<String, Object>>> getCVInfosByRecruitment(@PathVariable Integer recruitmentId) {
        List<Map<String, Object>> cvInfos = cvInfoService.getCVInfosByRecruitmentId(recruitmentId);
        return ResponseEntity.ok(cvInfos);
    }
    
    /**
     * Phê duyệt một CV và thêm user vào class
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveCV(@RequestParam Integer fileId, @RequestParam Integer recruitmentId) {
        Map<String, Object> result = cvInfoService.approveCV(fileId, recruitmentId);
        
        if ((Boolean) result.getOrDefault("success", false)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/reject")
    public ResponseEntity<?> deleteCVInfo(@RequestParam Integer fileId, @RequestParam Integer recruitmentId) {
        boolean result = cvInfoService.deleteCVInfo(fileId, recruitmentId);

        if (result) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa CVInfo thành công"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy CVInfo để xóa"));
        }
    }
    @GetMapping("/check-cv-info")
    public ResponseEntity<?> checkCvInfoExist(
            @RequestParam("fileId") Long fileId,
            @RequestParam("recruitmentId") Long recruitmentId) {
        try {
            boolean exists = cvInfoService.checkCvInfoExist(fileId, recruitmentId);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi khi kiểm tra thông tin CV: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

} 