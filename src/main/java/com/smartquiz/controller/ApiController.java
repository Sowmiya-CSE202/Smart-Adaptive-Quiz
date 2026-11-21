package com.smartquiz.controller;

import com.smartquiz.model.*;
import com.smartquiz.repository.*;
import com.smartquiz.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {

    private final PdfService pdf;
    private final QuestionGenerator generator;
    private final WordSearchGenerator puzzle;
    private final ContentStore store;

    private final QuestionAttemptRepository attempts;
    private final QuizSessionRepository sessions;

    private QuizSession currentSession;

    public ApiController(
            PdfService pdf,
            QuestionGenerator generator,
            WordSearchGenerator puzzle,
            ContentStore store,
            QuestionAttemptRepository attempts,
            QuizSessionRepository sessions) {

        this.pdf = pdf;
        this.generator = generator;
        this.puzzle = puzzle;
        this.store = store;
        this.attempts = attempts;
        this.sessions = sessions;
    }

    // PDF UPLOAD

    @PostMapping("/pdf/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file) {

        try {

            String text = pdf.extract(file);

            List<QuizQuestion> questions =
                    generator.generate(text);

            if (questions.isEmpty()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                            "error",
                            "Could not identify enough key terms in this PDF."
                        )
                );
            }

            List<String> words = questions.stream()
                    .map(QuizQuestion::answer)
                    .distinct()
                    .toList();

            char[][] grid = puzzle.generate(words);

            store.setContent(
                    text,
                    questions,
                    words,
                    grid
            );

            currentSession =
                    sessions.save(new QuizSession());

            return ResponseEntity.ok(
                    Map.of(
                        "message", "PDF processed successfully",
                        "charactersExtracted", text.length(),
                        "questionsGenerated", questions.size(),
                        "wordsFound", words
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                        "error",
                        e.getMessage() == null
                            ? "PDF processing failed"
                            : e.getMessage()
                    )
            );
        }
    }

    // FETCH QUIZ QUESTIONS

    @GetMapping("/quiz/questions")
    public ResponseEntity<?> questions() {

        if (store.getQuestions().isEmpty()) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", "Upload a PDF first.")
            );
        }

        return ResponseEntity.ok(
            store.getQuestions().stream()
                .map(q -> Map.of(
                    "id", q.id(),
                    "topic", q.topic(),
                    "prompt", q.prompt(),
                    "options", q.options(),
                    "difficulty",
                    store.getDifficulty(q.topic()).name()
                ))
                .toList()
        );
    }

    // SUBMIT ANSWER

    @PostMapping("/quiz/submit")
    public ResponseEntity<?> submit(
            @RequestBody Map<String, Object> body) {

        try {

            String id = String.valueOf(
                    body.get("questionId")
            );

            String answer = String.valueOf(
                    body.get("answer")
            );

            long responseMillis = Long.parseLong(
                    String.valueOf(
                        body.getOrDefault(
                            "responseMillis", "0"
                        )
                    )
            );

            QuizQuestion question =
                    store.getQuestions().stream()
                        .filter(q -> q.id().equals(id))
                        .findFirst()
                        .orElseThrow();

            boolean correct =
                    question.answer().equalsIgnoreCase(
                            answer.trim()
                    );

            Difficulty before =
                    store.getDifficulty(question.topic());

            store.updateDifficulty(
                    question.topic(),
                    correct
            );

            attempts.save(
                new QuestionAttempt(
                    question.topic(),
                    answer,
                    correct,
                    Math.max(0, responseMillis),
                    before.name()
                )
            );

            if (currentSession == null) {

                currentSession =
                        sessions.save(new QuizSession());
            }

            currentSession.record(correct);

            sessions.save(currentSession);

            return ResponseEntity.ok(
                Map.of(
                    "correct", correct,
                    "correctAnswer", question.answer(),
                    "difficulty",
                    store.getDifficulty(question.topic()).name(),
                    "message",
                    correct
                        ? "Correct answer!"
                        : "Incorrect answer. Review this concept."
                )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                        "error",
                        "Invalid quiz submission."
                    )
            );
        }
    }

    // WORD SEARCH PUZZLE

    @GetMapping("/puzzle")
    public ResponseEntity<?> getPuzzle() {

        if (store.getGrid().length == 0) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", "Upload a PDF first.")
            );
        }

        return ResponseEntity.ok(
            Map.of(
                "grid", store.getGrid(),
                "words", store.getWords()
            )
        );
    }

    // DASHBOARD STATISTICS

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        List<QuestionAttempt> all =
                attempts.findAll();

        long total = all.size();

        long correct = all.stream()
                .filter(QuestionAttempt::isCorrect)
                .count();

        double accuracy = total == 0
                ? 0
                : correct * 100.0 / total;

        double averageTime = total == 0
                ? 0
                : all.stream()
                    .mapToLong(
                        QuestionAttempt::getResponseMillis
                    )
                    .average()
                    .orElse(0);

        Map<String, List<QuestionAttempt>> byTopic =
                all.stream().collect(
                    Collectors.groupingBy(
                        QuestionAttempt::getTopic
                    )
                );

        Map<String, Double> topicAccuracy =
                new TreeMap<>();

        byTopic.forEach((topic, records) -> {

            double value = records.stream()
                    .filter(QuestionAttempt::isCorrect)
                    .count() * 100.0 / records.size();

            topicAccuracy.put(topic, value);
        });

        Map<String, Long> distribution = Map.of(
                "correct", correct,
                "incorrect", total - correct
        );

        List<Map<String, Object>> recent =
                attempts.findTop20ByOrderByAttemptedAtDesc()
                    .stream()
                    .map(a -> {

                        Map<String, Object> m =
                                new LinkedHashMap<>();

                        m.put("topic", a.getTopic());
                        m.put("answer", a.getAnswer());
                        m.put("correct", a.isCorrect());

                        m.put(
                            "responseMillis",
                            a.getResponseMillis()
                        );

                        m.put(
                            "attemptedAt",
                            a.getAttemptedAt().toString()
                        );

                        return m;
                    })
                    .toList();

        List<String> weakTopics =
                topicAccuracy.entrySet()
                    .stream()
                    .filter(e -> e.getValue() < 60)
                    .map(Map.Entry::getKey)
                    .toList();

        return Map.of(
            "questionsAttempted", total,
            "accuracy", Math.round(accuracy * 10.0) / 10.0,
            "averageTimeMillis", Math.round(averageTime),
            "topicsPracticed", topicAccuracy.size(),
            "topicAccuracy", topicAccuracy,
            "answerDistribution", distribution,
            "weakTopics", weakTopics,
            "recentActivity", recent
        );
    }

    // MISTAKE ANALYZER

    @GetMapping("/mistakes")
    public Map<String, Object> mistakes() {

        Map<String, Long> counts =
                attempts.findAll()
                    .stream()
                    .filter(a -> !a.isCorrect())
                    .collect(
                        Collectors.groupingBy(
                            QuestionAttempt::getTopic,
                            TreeMap::new,
                            Collectors.counting()
                        )
                    );

        return Map.of(
            "mistakesByTopic", counts,
            "totalMistakes",
            counts.values().stream()
                .mapToLong(Long::longValue)
                .sum()
        );
    }
}
