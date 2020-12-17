package ru.lanchukovskaya.sample;


import java.util.*;
import java.util.stream.Collectors;

public class Game implements Observable {
    private Map<Player, Snake> userList;
    private List<Cell> fruit;
    private List<Observer> observers;
    private List<Cell> emptySnake;
    private Field field;
    private SnakesProto.GameConfig config;
    private List<Player> isLose;
    private int stateId = 0;
    private Map<Cell, Status> CellFormerSnake = new HashMap();


    public Game(SnakesProto.GameConfig config) {
        this.config = config;
        field = new Field(config.getWidth(), config.getHeight());
        userList = new HashMap<>();
        observers = new ArrayList<>();
        fruit = new ArrayList<>();
        isLose = new ArrayList<>();
    }

    public Game(SnakesProto.GameState gameState) {
        this.config = gameState.getConfig();
        field = new Field(config.getWidth(), config.getHeight());
        userList = new HashMap<>();
        observers = new ArrayList<>();
        fruit = gameState.getFoodsList().stream()
                .map(ProtoUtils::getCellFromCoord)
                .peek(cell -> field.addCoordinates(cell.getX(), cell.getY(), Status.FRUIT))
                .collect(Collectors.toList());
        gameState.getPlayers().getPlayersList()
                .forEach(gamePlayer -> {
                    Player player = new Player(gamePlayer.getName(), gamePlayer.getId());
                    userList.put(player, null);
                });
        for (SnakesProto.GameState.Snake snake : gameState.getSnakesList()) {
            Snake snakeFromProto = ProtoUtils.getSnakeFromProto(snake);
            snakeFromProto.setHeight(config.getHeight());
            snakeFromProto.setWidth(config.getWidth());
            putSnakeByPlayerId(snakeFromProto, snake.getPlayerId());
        }
        isLose = new ArrayList<>();

    }

    private void putSnakeByPlayerId(Snake snakeFromProto, int playerId) {
        for (Player player : userList.keySet()) {
            if (player.getId() == playerId) {
                userList.put(player, snakeFromProto);
                break;
            }
        }
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

    public void logIn(Player pl) {
        Cell head = field.findTheFirstHeadPosition();
        if (headCheck(head)) {
            System.out.println("Sorry, there are too many players in this game");
        }
        Cell tail = getFirstTail(head);
        Snake sn = new Snake(head, tail, config.getWidth(), config.getHeight());
        userList.put(pl, sn);
        makeFruit();
    }

    public Map<Player, Snake> getUserList() {
        return userList;
    }


    public List<Player> getIsLose() {
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

    public List<Cell> getFruits() {
        return fruit;
    }

    public Cell getEmptySnake(int number) {
        return emptySnake.get(number);
    }

    public int getSizeEmptySnake() {
        return emptySnake.size();
    }

    public int getCountFruit() {
        return fruit.size();
    }


    private void makeFruit() {
        while ((field.numOfEmptyElField() > 0) && (fruit.size() < (userList.size() * config.getFoodPerPlayer() + config.getFoodStatic()))) {
            Cell cl = getCoordinate(Status.FRUIT);
            fruit.add(0, cl);
            field.addCoordinates(cl.getX(), cl.getY(), Status.FRUIT);
        }
    }

    private boolean snakeAteFruit(Cell head) {
        for (Cell cell : fruit) {
            if ((head.getX() == cell.getX()) && (head.getY() == cell.getY())) {
                return true;
            }
        }
        return false;
    }

    private boolean snakeCrashesIntoItself(Cell head, Player player) {
        Snake sn = userList.get(player);
        return sn.isBody(head);
    }

    private void delFruit(Cell head) {
        fruit.removeIf(
                cell -> (head.getX() == cell.getX()) && (head.getY() == cell.getY())
        );
    }

    public void win(Player player) {
        if ((userList.size() == 1 && field.numOfEmptyElField() == 1) || (userList.size() >= 2 && field.numOfEmptyElField() == 1 && userList.get(player).sizeSnake() > field.countOfElField() / userList.size())) {
            System.out.println("WIN");
        }
    }

    private void turningSnake(Player player) {
        for (int i = 1; i < userList.get(player).sizeSnake(); ++i) {
            player.decreaseScores();
            Status status = Status.EMPTY;
            if (Math.random() >= config.getDeadFoodProb()) {
                status = Status.FRUIT;
            }
            int x = userList.get(player).getSnakeCoordinate(i).getX();
            int y = userList.get(player).getSnakeCoordinate(i).getY();
            field.delCoordinates(x, y);
            field.addCoordinates(x, y, status);
            Cell newCell = new Cell(x, y);
            if (status == Status.FRUIT) {
                CellFormerSnake.put(newCell, status);
                fruit.add(userList.get(player).getSnakeCoordinate(i));
            }
            if (status == Status.EMPTY) {
                CellFormerSnake.put(newCell, status);
            }
            userList.get(player).removeCoordinate(i);
        }
        userList.get(player).removeCoordinate(0);
    }

    public Map<Cell, Status> getCellFormerSnake() {
        return CellFormerSnake;
    }

    private boolean crashIntoAnotherSnake(Cell head, Player player) {
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

    private void makeMove(Player player, Movement movement) {
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
        win(player);
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

    public void makeMove(Map<Player, Movement> movementMap) {
        userList.forEach((player, snake) -> {
            if (!movementMap.containsKey(player)) {
                makeMove(player, snake.getPrevDir());
            }
        });
        movementMap.forEach(this::makeMove);
        stateId++;
        notifyObservers();
    }

    public int getStateId() {
        return stateId;
    }

}
