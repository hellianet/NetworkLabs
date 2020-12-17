package ru.lanchukovskaya.sample;

public interface Observer {
    void update();

    void update(SnakesProto.GameState state);
}
