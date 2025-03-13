package com.fsoft.fintern.controllers;

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

    @GetMapping("/recruitments/{user_id}")
    public String listRecruitments(@PathVariable("user_id") int userId, Model model) {
        ResponseEntity<List<Recruitment>> response = recruitmentServices.findAll();

        model.addAttribute("user_id", userId);
        if (response.getBody() != null) {
            model.addAttribute("recruitments", response.getBody());
        } else {
            model.addAttribute("recruitments", List.of());
        }

        return "recruitment-list";
    }


    @GetMapping("/{id}/{user_id}")
    public String viewRecruitment(@PathVariable int id, @PathVariable int user_id, Model model) throws BadRequestException {
        Recruitment recruitment = recruitmentServices.findById(id).getBody();
        if (recruitment == null) {
            throw new BadRequestException("Recruitment not found");
        }
        model.addAttribute("recruitment", recruitment);
        model.addAttribute("user_id", user_id);
        return "recruitment-detail";
    }


    @PostMapping("/recruitment/create")
    @Operation(description = "Create a new recruitment")
    public ResponseEntity<Recruitment> create(@RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentServices.create(recruitmentDTO);
    }


    @DeleteMapping("/recruitment/delete/{id}")
    @Operation(description = "Delete Task by ID")
    public ResponseEntity<Recruitment> delete(@PathVariable int id) throws BadRequestException {
        return this.recruitmentServices.delete(id);
    }


    @PatchMapping("/recruitment/update/{id}")
    @Operation(description = "Update Task by Id")
    public ResponseEntity<Recruitment> update(@PathVariable int id, @RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentServices.update(id, recruitmentDTO);
    }

    @PatchMapping("/recruitment/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Recruitment> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.recruitmentServices.setIsActiveTrue(id);
    }




}