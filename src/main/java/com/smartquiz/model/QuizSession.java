package com.smartquiz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_sessions")
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;

    private int attempted;

    private int correct;

    public QuizSession() {

        this.startedAt = LocalDateTime.now();

    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public int getAttempted() {
        return attempted;
    }

    public int getCorrect() {
        return correct;
    }

    public void record(boolean ok) {

        attempted++;

        if (ok) {
            correct++;
        }
    }
}
