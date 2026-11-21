package com.smartquiz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private String answer;

    private boolean correct;

    private long responseMillis;

    private String difficulty;

    private LocalDateTime attemptedAt;

    public QuestionAttempt() {
    }

    public QuestionAttempt(
        String topic,
        String answer,
        boolean correct,
        long responseMillis,
        String difficulty
    ) {

        this.topic = topic;
        this.answer = answer;
        this.correct = correct;
        this.responseMillis = responseMillis;
        this.difficulty = difficulty;

        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public long getResponseMillis() {
        return responseMillis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
