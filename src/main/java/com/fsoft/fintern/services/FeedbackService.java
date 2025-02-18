package com.fsoft.fintern.services;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.FeedbackDTO;
import com.fsoft.fintern.models.Feedback;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.FeedbackRepository;
import com.fsoft.fintern.repositories.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class FeedbackService {
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;


    public FeedbackService (UserRepository userRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public ResponseEntity<Feedback> createFeedback(FeedbackDTO feedbackDTO) throws BadRequestException {
        User user = userRepository.findById(feedbackDTO.getCreated_by()).orElseThrow();
        if (user == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }
        Feedback feedback = new Feedback();

        feedback.setCreatedBy(user.getId());
        feedback.setContent(feedbackDTO.getContent());

        Feedback savedFeedback = this.feedbackRepository.save(feedback);
        return new ResponseEntity<>(savedFeedback, HttpStatus.CREATED);
    }




    public ResponseEntity<Feedback> setIsActiveTrue(int id) throws BadRequestException {
        Feedback existedFeedback = this.feedbackRepository.findById(id).orElse(null);
        if (existedFeedback == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.FEEDBACK_IS_NOT_FOUND.getMessage());
        }
        if (existedFeedback.isActive()) {
            throw new BadRequestException(ErrorDictionaryConstraints.IS_ACTIVE_TRUE.getMessage());
        }

        existedFeedback.setActive(true);
        feedbackRepository.save(existedFeedback);
        return new ResponseEntity<>(existedFeedback, HttpStatus.OK);
    }

    public ResponseEntity<Feedback> findById(int id) throws BadRequestException {
        Optional<Feedback> feedback = this.feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            return new ResponseEntity<>(feedback.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException(ErrorDictionaryConstraints.FEEDBACK_NOT_EXIST_ID.getMessage());
        }
    }

    public ResponseEntity<List<Feedback>> findAll() {
        List<Feedback> feedbacks = this.feedbackRepository.findAll();
        if (feedbacks.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(feedbacks, HttpStatus.OK);
        }
    }

    public ResponseEntity<Feedback> delete(int id) throws BadRequestException {
        Feedback feedback = this.feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.FEEDBACK_NOT_EXIST_ID.getMessage());
        }
        feedbackRepository.delete(feedback);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    private Feedback findByContentName(String content) {
        Optional<Feedback> feedback = this.feedbackRepository.findByContent(content);
        if (feedback.isPresent()) {
            return feedback.get();
        } else {
            return null;
        }
    }
}
