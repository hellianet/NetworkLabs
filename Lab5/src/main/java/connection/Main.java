package connection;

public class Main {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            new Thread(new Server(port))
                    .start();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
