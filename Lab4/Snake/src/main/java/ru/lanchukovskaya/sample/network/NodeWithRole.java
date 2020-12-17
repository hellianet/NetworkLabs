package ru.lanchukovskaya.sample.network;

import ru.lanchukovskaya.sample.Movement;
import ru.lanchukovskaya.sample.SnakesProto;

public interface NodeWithRole {

    void processMessage(Node node, SnakesProto.GameMessage message);

    void makeMove(Movement movement);

    void exit();
}
