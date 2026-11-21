package com.smartquiz.service;

import com.smartquiz.model.*;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

@Service
public class QuestionGenerator {

    private static final Set<String> STOP = Set.of(
        "THE", "AND", "FOR", "WITH", "FROM", "THAT",
        "THIS", "THEN", "THEY", "THEM", "WERE", "WAS",
        "ARE", "IS", "HAS", "HAVE", "HAD", "INTO",
        "ONTO", "WHEN", "WHERE", "WHICH", "WHAT",
        "WHY", "HOW", "CAN", "WILL", "SHOULD",
        "WOULD", "COULD", "YOUR", "YOU", "OUR",
        "THEIR", "ITS", "NOT", "BUT", "USE", "USED",
        "USING", "ALSO", "SUCH", "THAN", "MORE",
        "LESS", "OVER", "UNDER", "BETWEEN", "EACH",
        "OTHER", "SOME", "ANY", "ALL", "ONE",
        "TWO", "THREE"
    );

    private static final Set<String> TECH = Set.of(
        "JAVA", "PYTHON", "CLASS", "OBJECT", "METHOD",
        "INHERITANCE", "POLYMORPHISM", "ENCAPSULATION",
        "ABSTRACTION", "INTERFACE", "DATABASE",
        "ALGORITHM", "NETWORK", "SERVER", "CLIENT",
        "COMPUTER", "MEMORY", "PROCESSOR", "SOFTWARE",
        "HARDWARE", "FUNCTION", "ARRAY", "STACK",
        "QUEUE", "TREE", "GRAPH", "BINARY", "HTML",
        "CSS", "JAVASCRIPT", "SPRING", "CLOUD",
        "SECURITY", "PROTOCOL", "VARIABLE", "COMPILER",
        "OPERATING", "SYSTEM"
    );

    public List<QuizQuestion> generate(String text) {

        LinkedHashMap<String, String> selected =
                new LinkedHashMap<>();

        String[] sentences = text
                .replaceAll("\\s+", " ")
                .split("(?<=[.!?])\\s+");

        for (String sentence : sentences) {

            String clean = sentence.trim();

            if (clean.length() < 35) {
                continue;
            }

            Matcher matcher = Pattern.compile(
                "\\b[A-Za-z][A-Za-z0-9+#.-]{3,}\\b"
            ).matcher(clean);

            while (matcher.find()) {

                String raw = matcher.group();

                String key = raw.toUpperCase(Locale.ROOT)
                        .replaceAll("[^A-Z0-9]", "");

                if (key.length() < 4 ||
                    key.length() > 18 ||
                    STOP.contains(key)) {

                    continue;
                }

                boolean technical =
                        TECH.contains(key) ||
                        raw.matches("[A-Z][a-z]+[A-Z].*") ||
                        raw.equals(raw.toUpperCase(Locale.ROOT));

                if (technical && !selected.containsKey(key)) {

                    selected.put(key, clean);
                    break;
                }
            }
        }

        if (selected.isEmpty()) {

            for (String token : text.split("\\W+")) {

                String key =
                        token.toUpperCase(Locale.ROOT);

                if (key.length() >= 5 &&
                    key.length() <= 14 &&
                    !STOP.contains(key)) {

                    selected.putIfAbsent(
                            key,
                            "The uploaded document discusses "
                                    + token + "."
                    );
                }

                if (selected.size() >= 12) {
                    break;
                }
            }
        }

        List<QuizQuestion> result =
                new ArrayList<>();

        int number = 1;

        for (var entry : selected.entrySet()) {

            String word = entry.getKey();

            String sentence = entry.getValue();

            String masked = sentence.replaceAll(
                    "(?i)\\b" + Pattern.quote(word) + "\\b",
                    "________"
            );

            if (masked.equals(sentence)) {

                masked = "Identify the key concept: "
                        + sentence;
            }

            String topic = word.length() <= 5
                    ? "Core Concepts"
                    : word.substring(0, 1)
                      + word.substring(1).toLowerCase(
                            Locale.ROOT
                      );

            List<String> options = new ArrayList<>();

            options.add(word);

            for (String other : selected.keySet()) {

                if (!other.equals(word) &&
                    options.size() < 4) {

                    options.add(other);
                }
            }

            while (options.size() < 4) {
                options.add("NONE OF THESE");
            }

            Collections.shuffle(
                    options,
                    new Random(word.hashCode())
            );

            result.add(
                new QuizQuestion(
                    "Q" + number++,
                    topic,
                    masked,
                    word,
                    List.copyOf(options),
                    Difficulty.MEDIUM,
                    "Find the term described in the sentence."
                )
            );
        }

        return result;
    }
}
