package com.smartquiz.model;

import java.util.List;

public record QuizQuestion(

    String id,
    String topic,
    String prompt,
    String answer,
    List<String> options,
    Difficulty difficulty,
    String clue

) {}
