package ru.lanchukovskaya.sample;

public class Config {

    private int foodStatic = 1;
    private int foodPerPlayer = 2;
    private int heightField = 16;
    private int widthField = 15;
    private double percentChanceOfTurningIntoFood = 0.5;

    public Config() {

    }

    public int getFoodPerPlayer() {
        return foodPerPlayer;
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public double getPercentChanceOfTurningIntoFood() {
        return percentChanceOfTurningIntoFood;
    }

    public int getHeightField() {
        return heightField;
    }

    public int getWidthField() {
        return widthField;
    }
}
