package com.smartquiz.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordSearchGenerator {

    private static final int[][] DIRECTIONS = {

        {0, 1},
        {1, 0},
        {1, 1},
        {-1, 1},
        {0, -1},
        {-1, 0},
        {-1, -1},
        {1, -1}

    };

    public char[][] generate(List<String> words) {

        int longest = words.stream()
                .mapToInt(String::length)
                .max()
                .orElse(5);

        int size = Math.max(
                10,
                Math.min(22, longest + 3)
        );

        char[][] grid = new char[size][size];

        for (char[] row : grid) {
            Arrays.fill(row, ' ');
        }

        List<String> ordered =
                new ArrayList<>(words);

        ordered.sort(
                (a, b) -> Integer.compare(
                        b.length(), a.length()
                )
        );

        Random random = new Random(
                20261121L + words.hashCode()
        );

        for (String word : ordered) {

            String w = word.replaceAll(
                    "[^A-Z]", ""
            );

            if (w.isEmpty() || w.length() > size) {
                continue;
            }

            boolean placed = false;

            for (int attempt = 0;
                 attempt < 500 && !placed;
                 attempt++) {

                int[] direction =
                        DIRECTIONS[random.nextInt(
                                DIRECTIONS.length
                        )];

                int row = random.nextInt(size);
                int col = random.nextInt(size);

                int endRow = row
                        + (w.length() - 1) * direction[0];

                int endCol = col
                        + (w.length() - 1) * direction[1];

                if (endRow < 0 || endRow >= size ||
                    endCol < 0 || endCol >= size) {

                    continue;
                }

                boolean valid = true;

                for (int i = 0; i < w.length(); i++) {

                    char existing = grid[
                            row + i * direction[0]
                    ][
                            col + i * direction[1]
                    ];

                    if (existing != ' ' &&
                        existing != w.charAt(i)) {

                        valid = false;
                        break;
                    }
                }

                if (valid) {

                    for (int i = 0; i < w.length(); i++) {

                        grid[
                            row + i * direction[0]
                        ][
                            col + i * direction[1]
                        ] = w.charAt(i);

                    }

                    placed = true;
                }
            }
        }

        for (int r = 0; r < size; r++) {

            for (int c = 0; c < size; c++) {

                if (grid[r][c] == ' ') {

                    grid[r][c] = (char) (
                            'A' + random.nextInt(26)
                    );
                }
            }
        }

        return grid;
    }
}
