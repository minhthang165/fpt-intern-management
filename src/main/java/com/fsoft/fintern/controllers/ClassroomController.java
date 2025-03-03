package com.fsoft.fintern.controllers;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.services.ClassroomService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;

@Controller
@SessionAttributes("classroomList")
@RequestMapping("manage-class")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @GetMapping
    public String redirectManageClassPage(Model model) {
        try{
            ResponseEntity<List<Classroom>> response = classroomService.findAll();
            model.addAttribute("classroomList", response.getBody());
            return "Admin/ManageClass";
        } catch (BadRequestException e) {
            model.addAttribute(ErrorDictionaryConstraints.CLASS_IS_EMPTY.getMessage());
            return "Admin/AdminDashboard";
        }

    }
}
