package ru.lanchukovskaya.sample;

import java.util.List;
import java.util.stream.Collectors;

public class ProtoUtils {
    public static Cell getCellFromCoord(SnakesProto.GameState.Coord coord) {
        return new Cell(coord.getX(), coord.getY());
    }

    public static Snake getSnakeFromProto(SnakesProto.GameState.Snake snake) {
        Movement movement = getMovementByProto(snake.getHeadDirection());
        List<Cell> points = snake.getPointsList().stream().map(ProtoUtils::getCellFromCoord).collect(Collectors.toList());
        return new Snake(points, movement);
    }

    public static Movement getMovementByProto(SnakesProto.Direction direction) {
        switch (direction) {
            case UP:
                return Movement.UP;
            case DOWN:
                return Movement.DOWN;
            case LEFT:
                return Movement.LEFT;
            case RIGHT:
                return Movement.RIGHT;
        }
        return null;
    }

    public static SnakesProto.Direction getDirectionByMovement(Movement movement) {
        switch (movement) {
            case UP:
                return SnakesProto.Direction.UP;
            case DOWN:
                return SnakesProto.Direction.DOWN;
            case LEFT:
                return SnakesProto.Direction.LEFT;
            case RIGHT:
                return SnakesProto.Direction.RIGHT;
        }
        return null;
    }
}
