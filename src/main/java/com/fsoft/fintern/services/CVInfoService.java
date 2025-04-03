package com.fsoft.fintern.services;

import com.fsoft.fintern.models.*;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.repositories.CVInfoRepository;
import com.fsoft.fintern.repositories.FileRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.repositories.RecruitmentRepository;
import com.fsoft.fintern.repositories.ClassroomRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class CVInfoService {
    private final CVInfoRepository cvInfoRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private int currentIndex = 0;
    private List<Recruitment> activeRecruitmentsCache = new ArrayList<>();
    private final ClassroomRepository classroomRepository;
    private final EmailService mailService;
    private final FileService fileService;
    private static final Logger logger = LoggerFactory.getLogger(CVInfoService.class);
    @Autowired
    public CVInfoService(CVInfoRepository cvInfoRepository, FileRepository fileRepository,
                         UserRepository userRepository, RecruitmentRepository recruitmentRepository,

                         ClassroomRepository classroomRepository, EmailService mailService, FileService fileService) {
        this.cvInfoRepository = cvInfoRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.recruitmentRepository = recruitmentRepository;

        this.classroomRepository = classroomRepository;
        this.mailService = mailService;
        this.fileService = fileService;
    }


    @Transactional
    public CVInfo create(CVInfoId id, Double gpa, String skill, String education) {
        if (cvInfoRepository.existsById(id)) {
            throw new RuntimeException("CV đã tồn tại!");
        }
        CVInfo cvInfo = new CVInfo();
        cvInfo.setId(id);
        cvInfo.setGpa(gpa);
        cvInfo.setSkill(skill);
        cvInfo.setEducation(education);
        return cvInfoRepository.save(cvInfo);
    }
    
    /**
     * Lấy danh sách CV_Info theo recruitmentId kèm thông tin từ File và User
     * Chỉ lấy những CV có isActive = true
     */
    public List<Map<String, Object>> getCVInfosByRecruitmentId(Integer recruitmentId) {
        List<CVInfo> cvInfos = cvInfoRepository.findAllByRecruitmentIdAndUserRoleGuest(recruitmentId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (CVInfo cvInfo : cvInfos) {
            Map<String, Object> cvInfoData = new HashMap<>();
            cvInfoData.put("gpa", cvInfo.getGpa());
            cvInfoData.put("skill", cvInfo.getSkill());
            cvInfoData.put("education", cvInfo.getEducation());
            cvInfoData.put("fileId", cvInfo.getId().getFileId());
            cvInfoData.put("recruitmentId", cvInfo.getId().getRecruitmentId());
            
            // Lấy thông tin File
            Optional<File> fileOptional = fileRepository.findById(cvInfo.getId().getFileId());
            if (fileOptional.isPresent()) {
                File file = fileOptional.get();
                // Lấy thông tin User (người nộp CV)
                User submitter = file.getSubmitter();
                if (submitter != null) {
                    cvInfoData.put("name", submitter.getFirst_name() + " " + submitter.getLast_name());
                    cvInfoData.put("gender", submitter.getGender());
                } else {
                    cvInfoData.put("name", "Unknown");
                    cvInfoData.put("gender", "MALE");
                }
            } else {
                cvInfoData.put("name", "Unknown");
                cvInfoData.put("gender", "MALE");
            }
            
            result.add(cvInfoData);
        }
        
        return result;
    }

    /**
     * Phê duyệt CV và thêm người dùng vào lớp học
     * @param fileId ID của file CV
     * @param recruitmentId ID của yêu cầu tuyển dụng
     * @return Thông tin về trạng thái tuyển dụng
     */
    @Transactional
    public Map<String, Object> approveCV(Integer fileId, Integer recruitmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Lấy thông tin recruitment
            Recruitment recruitment = recruitmentRepository.findByIdAndIsActiveTrue(recruitmentId);
            if (recruitment == null) {
                throw new RuntimeException("Không tìm thấy yêu cầu tuyển dụng");
            }
            
            // 2. Lấy thông tin classroom
            Optional<Classroom> classroomOptional = classroomRepository.findById(recruitment.getClassId());
            if (classroomOptional.isEmpty()) {
                throw new RuntimeException("Không tìm thấy lớp học");
            }
            Classroom classroom = classroomOptional.get();
            
            // 3. Lấy thông tin user từ file
            Optional<File> fileOptional = fileRepository.findById(fileId);
            if (fileOptional.isEmpty()) {
                throw new RuntimeException("Không tìm thấy file CV");
            }
            
            User user = fileOptional.get().getSubmitter();
            if (user == null) {
                throw new RuntimeException("Không tìm thấy thông tin người dùng");
            }

            //4. Check user has been added into class
            if(user.getClassroom() != null)
            {
                result.put("success", false);
                result.put("message", "User has been approved into another class");
                return result;
            }

            // 5. Cập nhật role của user thành INTERN nếu là GUEST
            if (user.getRole() == Role.GUEST) {
                user.setRole(Role.INTERN);
            }
            
            // 6. Thêm user vào lớp học
            user.setClassroom(classroom);
            userRepository.save(user);
            
            // 7. Cập nhật số lượng intern trong lớp
            classroom.setNumberOfIntern(classroom.getNumberOfIntern() + 1);
            classroomRepository.save(classroom);

            // 8. Đánh dấu CV hiện tại là inactive (đã được approve)
            CVInfoId cvInfoId = new CVInfoId(recruitmentId, fileId);
            Optional<CVInfo> currentCVInfoOptional = cvInfoRepository.findById(cvInfoId);
            if (currentCVInfoOptional.isPresent()) {
                CVInfo currentCVInfo = currentCVInfoOptional.get();
                currentCVInfo.setActive(false);
                cvInfoRepository.save(currentCVInfo);
            }

            result.put("position", recruitment.getPosition());
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    public boolean checkCvInfoExist(Long fileId, Long recruitmentId) {
        if (fileId == null || recruitmentId == null) {
            throw new IllegalArgumentException("fileId và recruitmentId không được để trống!");
        }

        // Chuyển Long thành Integer vì CVInfoId dùng Integer
        Integer fileIdInt = fileId.intValue();
        Integer recruitmentIdInt = recruitmentId.intValue();

        return cvInfoRepository.existsByIdFileIdAndIdRecruitmentId(fileIdInt, recruitmentIdInt);
    }
    @Transactional
    public boolean deleteCVInfo(Integer fileId, Integer recruitmentId) {
        try {
            CVInfoId cvInfoId = new CVInfoId(recruitmentId, fileId);
            Optional<CVInfo> cvInfoOptional = cvInfoRepository.findById(cvInfoId);
            if (cvInfoOptional.isPresent()) {
                cvInfoRepository.delete(cvInfoOptional.get());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa CVInfo: " + e.getMessage());
        }
    }
    // Kiểm tra xem recruitment có nên xóa CV hay không
    public boolean shouldDeleteCVs(Integer recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findByIdAndIsActiveTrue(recruitmentId);
        if (recruitment == null) {
            logger.info("Recruitment {} không tồn tại hoặc không active.", recruitmentId);
            return false;
        }

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        int totalSlot = recruitment.getTotalSlot();
        int cvCount = cvInfoRepository.countByRecruitmentIdAndIsActiveTrue(recruitmentId);
        int remainingSlot = totalSlot - cvCount;

        boolean isTimeEnded = recruitment.getEndTime().before(currentTime);
        boolean isSlotsFull = remainingSlot <= 0;

        return isTimeEnded || isSlotsFull;
    }

    // Hàm gửi email thông báo
    private void sendEmailNotification(Integer fileId, boolean isKept) {
        try {
            ResponseEntity<User> userResponse = fileService.findUserByFileId(fileId);
            User user = userResponse.getBody();
            if (user != null && user.getEmail() != null) {
                EmailDetails emailDetails = new EmailDetails();
                emailDetails.setRecipient(user.getEmail());

                if (isKept) {
                    emailDetails.setSubject("Thông báo: CV của bạn đã qua được vòng sàng lọc");
                    emailDetails.setMsgBody("Kính gửi " + user.getFirst_name() + " " + user.getLast_name() + ",\n\n" +
                            "CV của bạn cho yêu cầu tuyển dụng đã qua được vòng sàng lọc.\n" +
                            "Chúng tôi sẽ tiếp tục xem xét hồ sơ của bạn, vui lòng kiểm tra email liên tục để biết thời gian phỏng vấn.\n\n" +
                            "Trân trọng,\nĐội ngũ FPT Intern");
                } else {
                    emailDetails.setSubject("Thông báo: CV của bạn không đạt yêu cầu");
                    emailDetails.setMsgBody("Kính gửi " + user.getFirst_name() + " " + user.getLast_name() + ",\n\n" +
                            "Chúng tôi rất tiếc thông báo rằng CV của bạn không đạt yêu cầu cho đợt tuyển dụng này.\n" +
                            "Cảm ơn bạn đã quan tâm và nộp hồ sơ. Chúc bạn may mắn trong những cơ hội tiếp theo!\n\n" +
                            "Trân trọng,\nĐội ngũ FPT Intern");
                }

                String mailResult = mailService.sendMail(emailDetails);
                logger.info("Sent email to {} (CV {}): {}", user.getEmail(), isKept ? "kept" : "deleted", mailResult);
            } else {
                logger.warn("No user or email found for fileId {}", fileId);
            }
        } catch (Exception e) {
            logger.error("Error sending email for fileId {}: {}", fileId, e.getMessage());
        }
    }

    // Xóa CV và gửi email thông báo
    @Transactional
    public Map<String, Object> deleteCVInfosAboveMinGpaWhenTimeEndsOrSlotsFull(Integer recruitmentId) {
        Map<String, Object> result = new HashMap<>();
        List<Integer> deletedFileIds = new ArrayList<>();
        List<Integer> keptFileIds = new ArrayList<>();

        try {
            Recruitment recruitment = recruitmentRepository.findByIdAndIsActiveTrue(recruitmentId);
            if (recruitment == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy yêu cầu tuyển dụng");
                result.put("remainingSlot", 0);
                result.put("totalSlot", 0);
                result.put("cvCount", 0);
                return result;
            }

            int totalSlot = recruitment.getTotalSlot();
            int cvCount = cvInfoRepository.countByRecruitmentIdAndIsActiveTrue(recruitmentId);
            int remainingSlot = totalSlot - cvCount;

            // Kiểm tra điều kiện trước khi xóa
            if (!shouldDeleteCVs(recruitmentId)) {
                result.put("success", false);
                result.put("message", "Chưa đến thời gian kết thúc hoặc còn slot trống.");
                result.put("remainingSlot", remainingSlot);
                result.put("totalSlot", totalSlot);
                result.put("cvCount", cvCount);
                return result;
            }

            // Logic xóa khi điều kiện thỏa mãn
            result.put("totalSlot", totalSlot);
            result.put("cvCount", cvCount);

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            List<CVInfo> allCVs = cvInfoRepository.findAllByRecruitmentIdAndUserRoleGuest(recruitmentId);
            for (CVInfo cvInfo : allCVs) {
                deletedFileIds.add(cvInfo.getId().getFileId());
            }

            int deletedCount = cvInfoRepository.deleteCVInfosAboveMinGpaWhenTimeEndsOrSlotsFull(recruitmentId, currentTime);

            if (deletedCount > 0) {
                List<CVInfo> remainingCVs = cvInfoRepository.findAllByRecruitmentIdAndUserRoleGuest(recruitmentId);
                keptFileIds.clear();
                for (CVInfo cvInfo : remainingCVs) {
                    keptFileIds.add(cvInfo.getId().getFileId());
                }

                // Loại bỏ các fileId đã được giữ khỏi danh sách deletedFileIds
                deletedFileIds.removeAll(keptFileIds);

                cvCount = remainingCVs.size();
                remainingSlot = totalSlot - cvCount;
                result.put("cvCount", cvCount);
                recruitment.setActive(false);
                recruitmentRepository.save(recruitment);
                logger.info("Set isActive to false for recruitment {}", recruitmentId);
            } else {
                keptFileIds = new ArrayList<>(deletedFileIds);
                deletedFileIds.clear();
            }

            // Gửi email cho những người bị xóa CV
            for (Integer fileId : deletedFileIds) {
                sendEmailNotification(fileId, false);
            }

            // Gửi email cho những người không bị xóa CV
            for (Integer fileId : keptFileIds) {
                sendEmailNotification(fileId, true);
            }

            result.put("success", deletedFileIds.size() > 0);
            result.put("remainingSlot", remainingSlot);
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi khi xóa CVInfo: " + e.getMessage());
            result.put("remainingSlot", 0);
            result.put("totalSlot", 0);
            result.put("cvCount", 0);
            return result;
        }
    }

    // Lên lịch kiểm tra và xóa CV định kỳ
    @Scheduled(fixedRate = 1800000) // 30 phút
    public void checkAndDeleteOneRecruitment() {
        activeRecruitmentsCache = recruitmentRepository.findAllByIsActiveTrue();

        if (activeRecruitmentsCache.isEmpty()) {
            logger.info("No active recruitments to process.");
            currentIndex = 0;
            return;
        }

        if (currentIndex >= activeRecruitmentsCache.size()) {
            currentIndex = 0;
            logger.info("Reached end of active recruitments list. Looping back to start.");
        }

        Recruitment recruitment = activeRecruitmentsCache.get(currentIndex);
        Integer recruitmentId = recruitment.getId();

        if (shouldDeleteCVs(recruitmentId)) {
            try {
                Map<String, Object> result = deleteCVInfosAboveMinGpaWhenTimeEndsOrSlotsFull(recruitmentId);

                if ((boolean) result.get("success")) {
                    logger.info("Processed recruitment {}: Successfully deleted CVs. Total: {}, Remaining: {}, CV Count: {}",
                            recruitmentId, result.get("totalSlot"), result.get("remainingSlot"), result.get("cvCount"));
                } else {
                    logger.info("Processed recruitment {}: No CVs deleted. Total: {}, Remaining: {}, CV Count: {}, Message: {}",
                            recruitmentId, result.get("totalSlot"), result.get("remainingSlot"), result.get("cvCount"), result.get("message"));
                }
            } catch (Exception e) {
                logger.error("Error processing recruitment {}: {}", recruitmentId, e.getMessage());
            }
        } else {
            logger.info("Recruitment {} not eligible for CV deletion yet.", recruitmentId);
        }

        currentIndex++;
    }

}



