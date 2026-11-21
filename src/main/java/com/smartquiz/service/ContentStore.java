package com.smartquiz.service;

import com.smartquiz.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ContentStore {

    private volatile String extractedText = "";

    private volatile List<QuizQuestion> questions = List.of();

    private volatile List<String> words = List.of();

    private volatile char[][] grid = new char[0][0];

    private final Map<String, Difficulty> difficulty =
            new ConcurrentHashMap<>();

    private final Map<String, Integer> correctStreak =
            new ConcurrentHashMap<>();

    private final Map<String, Integer> wrongStreak =
            new ConcurrentHashMap<>();

    public synchronized void setContent(
            String text,
            List<QuizQuestion> q,
            List<String> w,
            char[][] g) {

        extractedText = text;
        questions = List.copyOf(q);
        words = List.copyOf(w);
        grid = g;

        for (QuizQuestion x : q) {
            difficulty.putIfAbsent(
                    x.topic(), Difficulty.MEDIUM
            );
        }
    }

    public String getExtractedText() {
        return extractedText;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public List<String> getWords() {
        return words;
    }

    public char[][] getGrid() {
        return grid;
    }

    public Difficulty getDifficulty(String topic) {

        return difficulty.getOrDefault(
                topic, Difficulty.MEDIUM
        );
    }

    public synchronized void updateDifficulty(
            String topic,
            boolean correct) {

        if (correct) {

            int streak = correctStreak.merge(
                    topic, 1, Integer::sum
            );

            wrongStreak.put(topic, 0);

            if (streak >= 3) {

                difficulty.put(
                        topic,
                        getDifficulty(topic).harder()
                );

                correctStreak.put(topic, 0);
            }

        } else {

            int streak = wrongStreak.merge(
                    topic, 1, Integer::sum
            );

            correctStreak.put(topic, 0);

            if (streak >= 2) {

                difficulty.put(
                        topic,
                        getDifficulty(topic).easier()
                );

                wrongStreak.put(topic, 0);
            }
        }
    }
}
