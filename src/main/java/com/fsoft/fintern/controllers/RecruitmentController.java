package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.dtos.RecruitmentDTO;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.services.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("recruitment")
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @Autowired
    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    @GetMapping("")
    @Operation(description = "view all Recruitment")
    public ResponseEntity<List<Recruitment>> viewAllRecruitment() {
        return this.recruitmentService.findAll();
    }

    @GetMapping("/recruitments")
    public String listRecruitments(@SessionAttribute("user") LoginUserDTO loginUserDTO, @RequestParam(name = "user_id", required = false) Integer user_id, Model model) {
        ResponseEntity<List<Recruitment>> response = recruitmentService.findAll();
        model.addAttribute("user_id", user_id);
        if (response.getBody() != null) {
            model.addAttribute("recruitments", response.getBody());
        } else {
            model.addAttribute("recruitments", List.of());
        }
        return "recruitment-page";
    }


    @GetMapping("/{id}")
    public String viewRecruitment(
            @PathVariable int id,
            @SessionAttribute("user") LoginUserDTO loginUserDTO,
            Model model) throws BadRequestException {

        Recruitment recruitment = recruitmentService.findById(id).getBody();
        if (recruitment == null) {
            throw new BadRequestException("Recruitment not found");
        }

        model.addAttribute("recruitment", recruitment);
//        model.addAttribute("user_id", user_id);
        return "recruitment-detail" ;
    }


    @PostMapping("/recruitment/create")
    @Operation(description = "Create a new recruitment")
    public ResponseEntity<Recruitment> create(@RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentService.create(recruitmentDTO);
    }


    @DeleteMapping("/recruitment/delete/{id}")
    @Operation(description = "Delete Task by ID")
    public ResponseEntity<Recruitment> delete(@PathVariable int id) throws BadRequestException {
        return this.recruitmentService.delete(id);
    }


    @PatchMapping("/recruitment/update/{id}")
    @Operation(description = "Update Task by Id")
    public ResponseEntity<Recruitment> update(@PathVariable int id, @RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentService.update(id, recruitmentDTO);
    }

    @PatchMapping("/recruitment/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Recruitment> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.recruitmentService.setIsActiveTrue(id);
    }




}