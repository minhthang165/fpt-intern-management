package com.fsoft.fintern.controllers;

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
@RequestMapping("api/recruitment")
public class RecruitmentController {
    private final RecruitmentService recruitmentServices;

    @Autowired
    public RecruitmentController(RecruitmentService recruitmentServices) {
        this.recruitmentServices = recruitmentServices;
    }

    @GetMapping("")
    @Operation(description = "view all Recruitment")
    public ResponseEntity<List<Recruitment>> viewAllRecruitment() {
        return this.recruitmentServices.findAll();
    }

    @GetMapping("/recruitments")
    public String listRecruitments(Model model) {
        ResponseEntity<List<Recruitment>> response = recruitmentServices.findAll();

        if (response.getBody() != null) {
            model.addAttribute("recruitments", response.getBody());
        } else {
            model.addAttribute("recruitments", List.of());
        }
        return "recruitment-page";
    }



    @GetMapping("/{id}/{user_id}")
    public String viewRecruitment(@PathVariable int id, @PathVariable int user_id, Model model) throws BadRequestException {
        Recruitment recruitment = recruitmentServices.findById(id).getBody();
        if (recruitment == null) {
            throw new BadRequestException("Recruitment not found");
        }
        model.addAttribute("recruitment", recruitment);
        model.addAttribute("user_id", user_id);
        return "recruitment-page" ;
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