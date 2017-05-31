package client;

import java.io.BufferedReader;
import java.io.IOException;
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
    private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {

        try {
            System.out.println("Enter your user name");
            userName = consoleReader.readLine();
            System.out.println("Enter password");
            userPassword = consoleReader.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Client.connect();
    }

    public static void connect() {
        host = "localhost";
        port = 31;

        try {
            Socket socket = new Socket(host, port);
            Thread.sleep(2000);
            String query;
            PrintWriter messageSender = new PrintWriter(socket.getOutputStream());
            messageSender.println(userName + "_ _" + userPassword);
            messageSender.flush();
            while (!(query = consoleReader.readLine()).equals("exit")) {
                System.out.println("Sending \"" + query + "\"...");

                messageSender.println(query);
                messageSender.flush();

            }
            System.out.println("Exit command");
            messageSender.close();

            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
