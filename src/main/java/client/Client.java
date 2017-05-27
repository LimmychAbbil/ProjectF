package client;

/**
 * Created by Limmy on 27.05.2017.
 */
public class Client {
    private static String host;
    private static String port;
    private static String userName;
    private static String userPassword;
    public static void main(String[] args) {
        Client.connect();
    }

    public static void connect() {
        host = "localhost";
        port = ""; //???
        userName = "User 1";
        userPassword = "1";
    }
}
