package com.smartquiz.repository;

import com.smartquiz.model.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionAttemptRepository
        extends JpaRepository<QuestionAttempt, Long> {

    List<QuestionAttempt> findTop20ByOrderByAttemptedAtDesc();

    List<QuestionAttempt> findByTopic(String topic);

}
