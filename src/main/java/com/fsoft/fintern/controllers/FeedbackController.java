package com.fsoft.fintern.controllers;


import com.fsoft.fintern.dtos.FeedbackDTO;
import com.fsoft.fintern.models.Feedback;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/feedback")
public class FeedbackController {
    private FeedbackService feedbackService;


    @Autowired
    public FeedbackController(FeedbackService feedbackService) { this.feedbackService = feedbackService;
    }

    @GetMapping("/feedback")
    @Operation(description = "View all Feedback")
   public ResponseEntity<List<Feedback>> findAll() {
        return this.feedbackService.findAll();
    }

    @PostMapping("/feedback/create")
    @Operation(description = "Create a new Feedback")
public ResponseEntity<Feedback> create(@RequestBody FeedbackDTO feedbackDTO) throws BadRequestException {
    return this.feedbackService.createFeedback(feedbackDTO);
    }


    @GetMapping("feedback/{id}")
    @Operation(description = "Get Feedback by ID")
    public ResponseEntity<Feedback> findById(@PathVariable int id) throws BadRequestException {
        return this.feedbackService.findById(id);
    }
    @PatchMapping("/feedback/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Feedback> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.feedbackService.setIsActiveTrue(id);
    }

    @DeleteMapping("/feedback/delete/{id}")
    @Operation(description = "Delete FeedBack by ID")
    public ResponseEntity<Feedback> delete(@PathVariable int id) throws BadRequestException {
        return this.feedbackService.delete(id);
    }
}
