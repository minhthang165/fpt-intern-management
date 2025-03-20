package com.fsoft.fintern.controllers;


import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.File;
import com.fsoft.fintern.repositories.FileRepository;
import com.fsoft.fintern.services.CVInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class CVInfoController {
    @Autowired
    private CVInfoService cvInfoService;
    @Autowired
    private FileRepository fileRepository;

    @GetMapping("/candidate-list")
    public String getCandidateList(
            @RequestParam("fileId") Integer fileId,
            @RequestParam("recruitmentId") Integer recruitmentId,
            Model model) {
        CVInfo cvInfo = cvInfoService.getCVInfoByFileIdAndRecruitmentId(fileId, recruitmentId);
        List<CVInfo> cvInfos = cvInfo != null ? Collections.singletonList(cvInfo) : Collections.emptyList();

        // Lấy thông tin File dựa trên fileId
        File file = fileRepository.findById(fileId).orElse(null);

        model.addAttribute("cvInfos", cvInfos);
        model.addAttribute("file", file); // Truyền File vào model
        model.addAttribute("selectedFileId", fileId);
        model.addAttribute("selectedRecruitmentId", recruitmentId);
        return "candidate-list";
    }
}
