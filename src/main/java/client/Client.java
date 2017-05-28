package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))){
            Socket socket = new Socket(host, port);
            Thread.sleep(2000);
            String query;
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            while (!(query = consoleReader.readLine()).equals("exit")) {
                System.out.println("Sending \"" + query + "\"...");

                writer.println(query);
                writer.flush();

            }
            System.out.println("Exit command");
            writer.close();

            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
