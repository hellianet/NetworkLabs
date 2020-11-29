package sample;

public class Field {
    private int numOfEmptyEl;
    private int widthField;
    private int heightField;
    private Status[] matrix;

    public Field(int width, int height) {
        widthField = width;
        heightField = height;
        numOfEmptyEl = width * height;
        matrix = new Status[width * height];
        for (int i = 0; i < width * height; ++i) {
            matrix[i] = Status.EMPTY;
        }
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

