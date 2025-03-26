package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import com.fsoft.fintern.models.File;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.repositories.CVInfoRepository;
import com.fsoft.fintern.repositories.FileRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.repositories.RecruitmentRepository;
import com.fsoft.fintern.repositories.RecruitmentRequestRepository;
import com.fsoft.fintern.repositories.ClassroomRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CVInfoService {
    private final CVInfoRepository cvInfoRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentRequestRepository recruitmentRequestRepository;
    private final ClassroomRepository classroomRepository;

    @Autowired
    public CVInfoService(CVInfoRepository cvInfoRepository, FileRepository fileRepository, 
                        UserRepository userRepository, RecruitmentRepository recruitmentRepository, 
                        RecruitmentRequestRepository recruitmentRequestRepository,
                        ClassroomRepository classroomRepository) {
        this.cvInfoRepository = cvInfoRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.recruitmentRequestRepository = recruitmentRequestRepository;
        this.classroomRepository = classroomRepository;
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
        List<CVInfo> cvInfos = cvInfoRepository.findAllByRecruitmentId(recruitmentId);
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
            
            // 2. Lấy classId từ recruitment_request
            Integer classId = recruitmentRequestRepository.findClassIdByRecruitmentId(recruitmentId);
            if (classId == null) {
                throw new RuntimeException("Không tìm thấy lớp cho yêu cầu tuyển dụng này");
            }
            
            // 3. Lấy thông tin classroom
            Optional<Classroom> classroomOptional = classroomRepository.findById(classId);
            if (classroomOptional.isEmpty()) {
                throw new RuntimeException("Không tìm thấy lớp học");
            }
            Classroom classroom = classroomOptional.get();
            
            // 4. Lấy thông tin user từ file
            Optional<File> fileOptional = fileRepository.findById(fileId);
            if (fileOptional.isEmpty()) {
                throw new RuntimeException("Không tìm thấy file CV");
            }
            
            User user = fileOptional.get().getSubmitter();
            if (user == null) {
                throw new RuntimeException("Không tìm thấy thông tin người dùng");
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
}

