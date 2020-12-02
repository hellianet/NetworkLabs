package ru.lanchukovskaya.sample;

public class Field {
    private int numOfEmptyEl;
    private int widthField;
    private int heightField;
    private Status[] matrix;
    private int searchBoxSize = 5;

    public Field(int width, int height) {
        widthField = width;
        heightField = height;
        numOfEmptyEl = width * height;
        matrix = new Status[width * height];
        for (int i = 0; i < width * height; ++i) {
            matrix[i] = Status.EMPTY;
        }
    }

    public Cell findTheFirstHeadPosition() {
        for (int i = 0; i < widthField * heightField; ++i) {
            if ((matrix[i] == Status.EMPTY) && (environmentCheck(i % widthField, i / widthField))) {
                return new Cell(i % widthField, i / widthField);
            }
        }
        return new Cell(-1, -1);
    }

    public boolean environmentCheck(int x, int y) {
        int aroundTheCenter = searchBoxSize / 2;
        for (int yCenter = -aroundTheCenter; yCenter <= aroundTheCenter; yCenter++) {
            for (int xCenter = -aroundTheCenter; xCenter <= aroundTheCenter; xCenter++) {
                int newX = checkingCoordinateX(x + xCenter);
                int newY = checkingCoordinateY(y + yCenter);
                if (matrix[newY * widthField + newX] == Status.SNAKE) {
                    return false;
                }
            }
        }
        return true;
    }

    public int checkingCoordinateX(int x) {
        if (x < 0) {
            x = widthField + x;
        }
        if (x >= widthField) {
            x = x - widthField;
        }
        return x;
    }

    public int checkingCoordinateY(int y) {
        if (y < 0) {
            y = heightField + y;
        }
        if (y >= heightField) {
            y = y - heightField;
        }
        return y;
    }


    public int getWidthField() {
        return widthField;
    }

    public int getHeightField() {
        return heightField;
    }

    public int numOfEmptyElField() {
        return numOfEmptyEl;
    }

    public int countOfElField() {
        return widthField * heightField;
    }

    public Status getField(int x, int y) {
        return matrix[y * widthField + x];
    }

    public void addCoordinates(int x, int y, Status status) {
        matrix[y * widthField + x] = status;
        numOfEmptyEl--;
    }

    public void delCoordinates(int x, int y) {
        matrix[y * widthField + x] = Status.EMPTY;
        numOfEmptyEl++;
    }

}

