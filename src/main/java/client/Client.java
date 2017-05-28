package client;

import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Limmy on 27.05.2017.
 */
public class Client {
    private static String host;
    private static int port;
    private static String userName;
    private static String userPassword;
    public static void main(String[] args) {

        Client.connect();
    }

    public static void connect() {
        host = "localhost";
        port = 31;
        userName = "User 1";
        userPassword = "1";

        try {
            Socket socket = new Socket(host, port);
            socket.sendUrgentData(20);
            Thread.sleep(2000);
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
