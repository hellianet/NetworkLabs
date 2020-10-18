import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {
    private final Socket clientSocket;
    private final InputStream reader;
    private final DataInputStream readingFromSocket;
    private final DataOutputStream writeToSocket;
    private String path;
    private int port;
    private InetAddress inetAddress;
    private final int FAILURE = 666;
    private final int SUCCESS = 777;
    private final int BUFFER_SIZE = 1024;

    Client(String path, int port, InetAddress inetAddress) throws IOException {
        this.path = path;
        this.port = port;
        this.inetAddress = inetAddress;
        clientSocket = new Socket(inetAddress, port);
        writeToSocket = new DataOutputStream(clientSocket.getOutputStream());
        readingFromSocket = new DataInputStream(clientSocket.getInputStream());
        reader = Files.newInputStream(Paths.get(path));
    }

    public void startWorkWithClient()  {
        try(clientSocket; reader; readingFromSocket; writeToSocket) {
            writeToSocket.writeInt(Paths.get(path).getFileName().toString().length());
            writeToSocket.writeUTF(Paths.get(path).getFileName().toString());
            long fileSize = Files.size(Paths.get(path));
            writeToSocket.writeLong(fileSize);
            long countOfBytes = 0;
            byte[] buf = new byte[BUFFER_SIZE];
            while (countOfBytes < fileSize) {
                int a = reader.read(buf);
                countOfBytes += a;
                writeToSocket.write(buf, 0, a);

            }
            if (readingFromSocket.readInt() == SUCCESS) {
                System.out.println("Data transfer was successful!");
            } else {
                System.out.println("Data transfer failed!");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
