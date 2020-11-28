package sample;

public class Field {
    private int numOfEmptyEl;
    private int sizeField;
    private Status[] matrix;

    public Field(int size) {
        sizeField = size;
        numOfEmptyEl = size * size;
        matrix = new Status[size * size];
        for (int i = 0; i < size * size; ++i) {
            matrix[i] = Status.EMPTY;
        }
    }

    public int sizeField() {
        return sizeField;
    }

    public int numOfEmptyElField() {
        return numOfEmptyEl;
    }

    public int countOfElField() {
        return sizeField * sizeField;
    }

    public Status getField(int x, int y) {
        return matrix[y * sizeField + x];
    }

    public void addCoordinates(int x, int y, Status status) {
        matrix[y * sizeField + x] = status;
        numOfEmptyEl--;
    }

    public void delCoordinates(int x, int y) {
        matrix[y * sizeField + x] = Status.EMPTY;
        numOfEmptyEl++;
    }

}

