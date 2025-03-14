package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.dtos.RecruitmentDTO;
import com.fsoft.fintern.dtos.TaskDTO;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.services.RecruitmentServices;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/recruitment")
public class RecruitmentController {
    private final RecruitmentServices recruitmentServices;

    public RecruitmentController(RecruitmentServices recruitmentServices) {
        this.recruitmentServices = recruitmentServices;
    }

    @GetMapping("/recruitments")
    public String listRecruitments(@SessionAttribute("user") LoginUserDTO loginUserDTO, @RequestParam(name = "user_id", required = false) Integer user_id, Model model) {
        ResponseEntity<List<Recruitment>> response = recruitmentServices.findAll();
        model.addAttribute("user_id", user_id);
        if (response.getBody() != null) {
            model.addAttribute("recruitments", response.getBody());
        } else {
            model.addAttribute("recruitments", List.of());
        }

        return "recruitment-list";
    }


    @GetMapping("/{id}")
    public String viewRecruitment(
            @PathVariable int id,
            @SessionAttribute("user") LoginUserDTO loginUserDTO,
            Model model) throws BadRequestException {

        Recruitment recruitment = recruitmentServices.findById(id).getBody();
        if (recruitment == null) {
            throw new BadRequestException("Recruitment not found");
        }

        model.addAttribute("recruitment", recruitment);
        model.addAttribute("user_id", loginUserDTO.getId());

        return "recruitment-detail";
    }






}