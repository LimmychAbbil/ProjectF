package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
    //TODO generate hash instead of sending the whole files' content
    private static String generateFilesCheckSummary(String fileNames) throws IOException {
        StringBuilder filesCheckSummary = new StringBuilder();
        String[] fileNamesArray = fileNames.split("\n");
        for (String fileName:fileNamesArray) {
            Path p = Paths.get("src/main/resources/client.examples/" + fileName);
            if (!Files.exists(p)) {
                System.out.println("Files doesn't exists " + p.getFileName());
                continue;
            }
            BufferedReader fileReader = new BufferedReader(new FileReader(p.toFile()));
            while (fileReader.ready()) {
                filesCheckSummary.append(fileReader.readLine()).append("\n");
            }

            fileReader.close();
        }
        return filesCheckSummary.toString().trim();
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
                filesToCheck.append(socketInput.readLine()).append("\n");
            }
            messageSender.println(generateFilesCheckSummary(filesToCheck.toString()));
            messageSender.flush();

            Thread.sleep(200);
            StringBuilder filesToReplace = new StringBuilder();
            while (socketInput.ready()) {
                filesToReplace.append(socketInput.readLine()).append("\n");
            }
            messageSender.println(generateFilesCheckSummary(filesToReplace.toString()));
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
