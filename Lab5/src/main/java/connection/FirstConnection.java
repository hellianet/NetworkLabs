package connection;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class FirstConnection extends StageConnection {

    private int answerWrittenAll = 0;

    private ByteBuffer buffer = ByteBuffer.wrap(new byte[257]);

    private byte[] answer = new byte[2];

    public FirstConnection(ConnectionSelector connectionSelector, SocketChannel channel) {
        super(connectionSelector, channel);
        System.out.println("FIRST PHASE CTOR");
    }

    @Override
    public void make(SelectionKey key) throws IOException {
        if (key.isValid() && key.isReadable()) {
            int read = channel.read(buffer);
            if (read == -1) {
                finish(key);
                return;
            }
            if (!checkReading()) return;
            if (buffer.get(0) != 0x05) {
                finish(key);
            }
            boolean hasAuth = false;
            for (int i = 2; i < buffer.position(); ++i) {
                if (buffer.get(i) == 0x00) {
                    hasAuth = true;
                    break;
                }
            }
            makeAnswer(hasAuth);
        }

        if (key.isValid() && key.isWritable()) {
            int written = channel.write(ByteBuffer.wrap(answer));
            answerWrittenAll += written;

            if (answerWrittenAll != answer.length) {
                return;
            }
            SecondConnection secondPhaseConnection = new SecondConnection(connectionSelector, channel);
            connectionSelector.registerConnection(channel, secondPhaseConnection, SelectionKey.OP_READ);
        }
    }

    private boolean checkReading() {
        if (buffer.position() < 1) return false;
        int length = buffer.get(1);
        return buffer.position() >= 2 + length;
    }

    private void makeAnswer(boolean hasAuth) {
        connectionSelector.registerConnection(channel, this, SelectionKey.OP_WRITE);
        answer[0] = 0x05;
        answer[1] = (byte) ((hasAuth) ? 0x00 : 0xFF);
    }

    @Override
    public void finish(SelectionKey key) {
        super.finish(key);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        connectionSelector.deleteConnection(channel);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
