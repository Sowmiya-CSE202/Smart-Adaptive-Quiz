package com.smartquiz.model;

public enum Difficulty {

    EASY,
    MEDIUM,
    HARD;

    public Difficulty harder() {

        return this == EASY ? MEDIUM :
               this == MEDIUM ? HARD : HARD;
    }

    public Difficulty easier() {

        return this == HARD ? MEDIUM :
               this == MEDIUM ? EASY : EASY;
    }
}
