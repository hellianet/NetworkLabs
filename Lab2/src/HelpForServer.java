import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class HelpForServer  extends Thread{
    private final Socket socket;
    private final DataInputStream readingFromSocket;
    private final DataOutputStream writeToSocket;
    private final int FAILURE = 666;
    private final int SUCCESS = 777;
    private final int SPLIT_TIME = 3000;
    private final int BUFFER_SIZE = 1024;

    HelpForServer(Socket socket) throws IOException {
        this.socket = socket;
        writeToSocket = new DataOutputStream(socket.getOutputStream());
        readingFromSocket = new DataInputStream(socket.getInputStream());
        start();
    }

    @Override
    public void run() {
        try (socket; writeToSocket; readingFromSocket){
            int sizeFileName = readingFromSocket.readInt();
            String fileName = readingFromSocket.readUTF();
            if(sizeFileName != fileName.length()){
                System.out.println("Еrror in transferring file name");
            }
            long sizeFile = readingFromSocket.readLong();
            Path currentPath = Paths.get("." + "\\uploads").toAbsolutePath();
            if (!Files.exists(currentPath)) {
                String newDir = Files.createDirectory(currentPath).toString();
            }
            String pathNewFile = "." + "\\uploads\\" + fileName;
            Path newFile = Files.createFile(Paths.get(pathNewFile));
            long speed = 0;
            long speedForAverage = 0;
            int iteration = 0;
            long countOfBytes = 0;

            byte[] buf = new byte[BUFFER_SIZE];
            long nowTime = System.currentTimeMillis();
            long timeAfterTransmission = 0;
            long currentNumber = 0;

            while(countOfBytes < sizeFile){
                int readBytes = readingFromSocket.read(buf);
                currentNumber += readBytes;
                countOfBytes += readBytes;
                timeAfterTransmission = System.currentTimeMillis();
                if((timeAfterTransmission - nowTime) >= SPLIT_TIME){
                    speed = (currentNumber * 1000) / ((timeAfterTransmission - nowTime));
                    speedForAverage += speed;
                    System.out.println("Instant speed = " + speed);
                    currentNumber = 0;
                    nowTime = timeAfterTransmission;
                    iteration++;
                    System.out.println("Average speed = " + speedForAverage/iteration);
                }

                Files.write(newFile,buf, StandardOpenOption.APPEND);
            }
            if((timeAfterTransmission - nowTime) < SPLIT_TIME){
                speed = (currentNumber * 1000) / ((timeAfterTransmission - nowTime));
                speedForAverage += speed;
                iteration++;
                System.out.println("Instant speed = " + speed);
                System.out.println("Average speed = " + speedForAverage/iteration);
            }

             if(sizeFile != countOfBytes){
                 writeToSocket.writeInt(FAILURE);
             }else{
                 writeToSocket.writeInt(SUCCESS);
             }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}