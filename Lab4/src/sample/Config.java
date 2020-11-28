package sample;

public class Config {

    private int foodStatic = 1;
    private int foodPerPlayer = 2;
    private int percentChanceOfTurningIntoFood = 50;

    public Config() {

    }

    public int getFoodPerPlayer() {
        return foodPerPlayer;
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public int getPercentChanceOfTurningIntoFood() {
        return percentChanceOfTurningIntoFood;
    }
}
