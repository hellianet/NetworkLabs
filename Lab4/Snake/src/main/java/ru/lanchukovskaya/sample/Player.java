package ru.lanchukovskaya.sample;

public class Player {
    private String myName;
    private Integer scores = 2;

    public Player(String name) {
        myName = name;
    }

    public String getName() {
        return myName;
    }

    public Integer getScores() {
        return scores;
    }

    public void increaseScores() {
        scores++;
    }

    public void decreaseScores() {
        scores--;
    }
}
