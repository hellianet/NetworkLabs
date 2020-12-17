package ru.lanchukovskaya.sample;

import java.util.Random;

public class Player {
    private static Random random = new Random();
    private String myName;
    private int scores = 2;
    private int id = 1;

    public Player(String name) {
        myName = name;
        id = random.nextInt();
    }

    public Player(String name, int id) {
        myName = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return myName;
    }

    public int getScores() {
        return scores;
    }

    public void increaseScores() {
        scores++;
    }

    public void decreaseScores() {
        scores--;
    }
}
