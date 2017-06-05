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

        Client.connect();
    }

    private static void inputCredentials() {
        try {
            System.out.println("Enter your user name");
            userName = consoleReader.readLine();
            System.out.println("Enter password");
            userPassword = consoleReader.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateFilesCheckSummary(String fileNames) {
        //TODO it is a stub
        return "someSummary";
    }


    public static void connect() {
        host = "localhost";
        port = 31;

        try (Socket socket = new Socket(host, port);
             PrintWriter messageSender = new PrintWriter(socket.getOutputStream());
             BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            Thread.sleep(2000);
            boolean isAuthorized = false;
            boolean fileCheckCompleted = false;
            while (!isAuthorized) {
                inputCredentials();
                messageSender.println(userName + "_ _" + userPassword);
                messageSender.flush();
                Thread.sleep(200);
                isAuthorized = socketInput.readLine().equals("Success");
            }

            Thread.sleep(200);
            StringBuilder filesToCheck = new StringBuilder();
            while (socketInput.ready()) {
                filesToCheck.append(socketInput.readLine());
            }
            System.out.println(generateFilesCheckSummary(""));
            messageSender.println(generateFilesCheckSummary(filesToCheck.toString()));
            messageSender.flush();

            String query;
            while (!(query = consoleReader.readLine()).equals("exit")) {
                System.out.println("Sending \"" + query + "\"...");

                messageSender.println(query);
                messageSender.flush();

            }

            messageSender.println("exit");
            messageSender.flush();
            System.out.println("Exit command");
            messageSender.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
