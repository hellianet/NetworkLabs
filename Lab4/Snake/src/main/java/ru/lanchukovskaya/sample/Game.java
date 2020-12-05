package ru.lanchukovskaya.sample;


import java.util.*;

public class Game implements Observable {
    private HashMap<Player, Snake> userList;
    private ArrayList<Cell> fruit;
    private ArrayList<Observer> observers;
    private ArrayList<Cell> emptySnake;
    private Field field;
    private Config config;
    private HashMap<Player, Boolean> isEat;
    private HashMap<Player, Boolean> snakeIsFruit;
    private ArrayList<Player> isWin;
    private ArrayList<Player> isLose;
    private Timer timer;

    public Game() {
        config = new Config();
        field = new Field(config.getWidthField(), config.getHeightField());
        userList = new HashMap<>();
        observers = new ArrayList<>();
        fruit = new ArrayList<>();
        isEat = new HashMap<>();
        snakeIsFruit = new HashMap<>();
        isWin = new ArrayList<>();
        isLose = new ArrayList<>();
    }

    public Cell getCoordinate(Status status) {
        Random random = new Random();
        int y = random.nextInt(field.getHeightField());
        int x = random.nextInt(field.getWidthField());
        while (field.getField(x, y) != Status.EMPTY) {
            y = random.nextInt(field.getHeightField());
            x = random.nextInt(field.getWidthField());
        }
        Cell k = new Cell(x, y);
        field.addCoordinates(x, y, status);
        return k;
    }

    public Cell getFirstTail(Cell head) {
        int x = head.getX();
        int y = head.getY();
        int newXMinus = field.checkingCoordinateX(--x);
        int newXPlus = field.checkingCoordinateX(++x);
        int newYMinus = field.checkingCoordinateY(--y);
        int newYPlus = field.checkingCoordinateY(++y);
        Cell tail = null;
        if (field.getField(newXPlus, y) == Status.EMPTY) {
            tail = new Cell(newXPlus, y);
            field.addCoordinates(newXPlus, y, Status.SNAKE);
        } else if (field.getField(newXMinus, y) == Status.EMPTY) {
            tail = new Cell(newXMinus, y);
            field.addCoordinates(newXMinus, y, Status.SNAKE);
        } else if (field.getField(x, newYPlus) == Status.EMPTY) {
            tail = new Cell(x, newYPlus);
            field.addCoordinates(x, newYPlus, Status.SNAKE);
        } else if (field.getField(x, newYMinus) == Status.EMPTY) {
            tail = new Cell(x, newYMinus);
            field.addCoordinates(x, newYMinus, Status.SNAKE);
        }
        return tail;
    }

    public boolean headCheck(Cell head) {
        return (head.getX() == -1) || (head.getY() == -1);
    }

    public Player logIn(String name) {
        Player pl = new Player(name);
        Cell head = field.findTheFirstHeadPosition();
        if (headCheck(head)) {
            System.out.println("Sorry, there are too many players in this game");
        }
        Cell tail = getFirstTail(head);
        Snake sn = new Snake(head, tail, config.getWidthField(), config.getHeightField());
        userList.put(pl, sn);
        makeFruit();
        return pl;
    }

    public void run() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                for (Player pl : userList.keySet()) {
                    makeMove(pl, userList.get(pl).getPrevDir());
                }
                if (userList.size() == 0) {
                    timer.cancel();
                }
            }
        };
        timer.schedule(timerTask, 0, 500);
    }

    public void exit() {
        userList.clear();
        timer.cancel();
    }

    public Map<Player, Snake> getUserList() {
        return userList;
    }

    public ArrayList<Player> getIsWin() {
        return isWin;
    }

    public ArrayList<Player> getIsLose() {
        return isLose;
    }

    public int widthField() {
        return field.getWidthField();
    }

    public int heightField() {
        return field.getHeightField();
    }

    public Cell getHeadSnake(Player player) {
        return userList.get(player).getHead();
    }

    public Cell getTailSnake(Player player) {
        return userList.get(player).getTail();
    }

    public ArrayList<Cell> getFruit() {
        return fruit;
    }

    public boolean snakeIsEat(Player player) {
        return isEat.get(player);
    }

    public Cell getEmptySnake(int number) {
        return emptySnake.get(number);
    }

    public int getSizeEmptySnake() {
        return emptySnake.size();
    }

    public boolean snakeIsFruitNow(Player player) {
        return snakeIsFruit.get(player);
    }

    public int getCountFruit() {
        return fruit.size();
    }

    public void makeFruit() {
        while ((field.numOfEmptyElField() > 0) && (fruit.size() < (userList.size() * config.getFoodPerPlayer() + config.getFoodStatic()))) {
            Cell cl = getCoordinate(Status.FRUIT);
            fruit.add(0, cl);
            field.addCoordinates(cl.getX(), cl.getY(), Status.FRUIT);
        }
    }

    public boolean snakeAteFruit(Cell head) {
        for (Cell cell : fruit) {
            if ((head.getX() == cell.getX()) && (head.getY() == cell.getY())) {
                return true;
            }
        }
        return false;
    }

    public boolean snakeCrashesIntoItself(Cell head, Player player) {
        Snake sn = userList.get(player);
        return sn.isBody(head);
    }

    public void delFruit(Cell head) {
        fruit.removeIf(
                cell -> (head.getX() == cell.getX()) && (head.getY() == cell.getY())
        );
    }

    public void win(Player player) {
        if ((userList.size() == 1 && field.numOfEmptyElField() == 1) || (userList.size() >= 2 && field.numOfEmptyElField() == 1 && userList.get(player).sizeSnake() > field.countOfElField() / userList.size())) {
            isWin.add(player);
        }
    }

    public void turningSnake(Player player) {
        for (int i = 1; i < userList.get(player).sizeSnake(); ++i) {
            player.decreaseScores();
            Status status = Status.EMPTY;
            if (Math.random() >= config.getPercentChanceOfTurningIntoFood()) {
                status = Status.FRUIT;
            }
            int x = userList.get(player).getSnakeCoordinate(i).getX();
            int y = userList.get(player).getSnakeCoordinate(i).getY();
            field.delCoordinates(x, y);
            field.addCoordinates(x, y, status);
            if (status == Status.FRUIT) {
                snakeIsFruit.put(player, true);
                fruit.add(userList.get(player).getSnakeCoordinate(i));
            }
            if (status == Status.EMPTY) {
                emptySnake = new ArrayList<>();
                snakeIsFruit.put(player, false);
                emptySnake.add(userList.get(player).getSnakeCoordinate(i));
            }
            userList.get(player).removeCoordinate(i);
        }
        userList.get(player).removeCoordinate(0);
    }


    public boolean crashIntoAnotherSnake(Cell head, Player player) {
        for (Map.Entry<Player, Snake> entry : userList.entrySet()) {
            if (player != entry.getKey()) {
                if (entry.getValue().isBody(head)) {
                    player.increaseScores();
                    return true;
                }
            }
        }
        return false;
    }

    public void makeMove(Player player, Movement movement) {
        userList.get(player).traffic(movement);
        checkMove(player);
    }

    public void deletePlayer(Player pl) {
        turningSnake(pl);
        userList.remove(pl);
    }

    public void lose(Player pl) {
        deletePlayer(pl);
    }

    public void checkMove(Player player) {
        Cell head = userList.get(player).getHead();
        int x = head.getX();
        int y = head.getY();
        if ((snakeCrashesIntoItself(head, player) || crashIntoAnotherSnake(head, player))) {
            lose(player);
            isLose.add(player);
            notifyObservers();
            return;
        }
        boolean eat = false;
        if (snakeAteFruit(head)) {
            field.delCoordinates(x, y);
            delFruit(head);
            field.addCoordinates(x, y, Status.SNAKE);
            player.increaseScores();
            makeFruit();
            eat = true;

        } else {
            field.addCoordinates(x, y, Status.SNAKE);
            Cell tail = userList.get(player).getTail();
            userList.get(player).removeTail();
            field.delCoordinates(tail.getX(), tail.getY());
        }
        isEat.put(player, eat);
        win(player);
        notifyObservers();
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}
