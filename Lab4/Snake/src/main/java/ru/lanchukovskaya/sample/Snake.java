package ru.lanchukovskaya.sample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Snake implements Iterable<Cell> {
    private List<Cell> snake;
    private Movement prevDir = Movement.LEFT;
    private int width;
    private int height;

    public Snake(Cell head, Cell tail, int w, int h) {
        snake = new ArrayList<>();
        snake.add(head);
        snake.add(tail);
        width = w;
        height = h;
    }

    public Cell getHead() {
        return snake.get(0);
    }

    public void removeTail() {
        snake.remove(snake.size() - 1);
    }

    public void removeCoordinate(int number) {
        snake.remove(number);
    }

    public Cell getTail() {
        return snake.get(snake.size() - 1);
    }

    public int sizeSnake() {
        return snake.size();
    }

    public Cell getSnakeCoordinate(int number) {
        return snake.get(number);
    }

    public boolean isBody(Cell point) {
        for (int i = 1; i < snake.size(); ++i) {
            if (snake.get(i).getX() == point.getX() && snake.get(i).getY() == point.getY()) {
                return true;
            }
        }
        return false;
    }

    public Movement getPrevDir() {
        return prevDir;
    }

    public Movement reverse() {
        if (prevDir == Movement.RIGHT) {
            return Movement.LEFT;
        }
        if (prevDir == Movement.LEFT) {
            return Movement.RIGHT;
        }
        if (prevDir == Movement.UP) {
            return Movement.DOWN;
        }
        return Movement.UP;
    }

    public void traffic(Movement move) {
        if (move == reverse()) {
            move = prevDir;
        }
        int x = 0;
        int y = 0;
        int endX = -1;
        int endY = -1;
        Cell newHead = null;
        if (move == Movement.DOWN) {
            if ((getHead().getY() + 1 == height)) {
                endY = 0;
                newHead = new Cell(snake.get(0).getX(), endY);
            } else {
                y++;
            }
        }
        if (move == Movement.UP) {
            if (getHead().getY() == 0) {
                endY = height - 1;
                newHead = new Cell(snake.get(0).getX(), endY);
            } else {
                y--;
            }
        }
        if (move == Movement.LEFT) {
            if (getHead().getX() == 0) {
                endX = width - 1;
                newHead = new Cell(endX, snake.get(0).getY());
            } else {
                x--;
            }
        }
        if (move == Movement.RIGHT) {
            if (getHead().getX() + 1 == width) {
                endX = 0;
                newHead = new Cell(endX, snake.get(0).getY());
            } else {
                x++;
            }
        }
        prevDir = move;
        if (endX == -1 && endY == -1) {
            newHead = new Cell(snake.get(0).getX() + x, snake.get(0).getY() + y);
        }
        snake.add(0, newHead);
    }

    @Override
    public Iterator<Cell> iterator() {
        return snake.iterator();
    }
}
