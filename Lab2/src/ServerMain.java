public class ServerMain {
    public static void main(String[] args) {
        Server sv = new Server();
        try {
            int port = Integer.parseInt(args[0]);
            sv.workServer(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }
}
