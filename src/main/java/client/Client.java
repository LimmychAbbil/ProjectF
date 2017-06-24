package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Limmy on 27.05.2017.
 */
public class Client {
    private static String host;
    private static int port;
    private static String userName;
    private static String userPassword;
    private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    private static List<Path> filesWithAlert = new ArrayList<>();
    private static List<Path> filesToReplace = new ArrayList<>();
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

    private static String generateFilesCheckSummary(List<Path> files) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        for (Path p: files) {
            if (Files.exists(p)) {
                try (FileReader fr = new FileReader(p.toFile())) {
                    while (fr.ready()) {
                        fileContent.append((char)fr.read());
                    }
                }
            }
        }
        return fileContent.toString().hashCode() + "";
    }


    private static void getListOfFiles(PrintWriter messageSender, BufferedReader socketInput) throws Exception{
        messageSender.println("sendlistoffiles");
        messageSender.flush();

        while (true) {
            String answerLine = socketInput.readLine();
            if (answerLine.isEmpty()) break;
            if (answerLine.startsWith("WARNING")) filesWithAlert.add(Paths.get("src/main/resources/client/examples/" + answerLine.split(":")[1]));
            if (answerLine.startsWith("CHECK")) filesToReplace.add(Paths.get("src/main/resources/client/examples/" + answerLine.split(":")[1]));
        }
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

            getListOfFiles(messageSender, socketInput);
            while (!isAuthorized) {
                inputCredentials();
                messageSender.println("Auth " + userName + " " + userPassword);
                messageSender.flush();
                Thread.sleep(200);
                String answer = socketInput.readLine();
                System.out.println(answer);
                isAuthorized = answer.equals("Success");
            }

            String s = generateFilesCheckSummary(filesWithAlert);
            messageSender.println("checkmywarningfiles " + s);
            messageSender.flush();

            System.out.println("Send my summary: " + s);


            /* StringBuilder filesToReplace = new StringBuilder();
            while (socketInput.ready()) {
                filesToReplace.append(socketInput.readLine()).append("\n");
            }
            messageSender.println(generateFilesCheckSummary(filesToReplace.toString()));
            messageSender.flush();

            Thread.sleep(2000);

            String s = socketInput.readLine();

                if (s.equals("Files wasn't changed")) {}
                else {
                    System.out.println("Files will be reloaded");
                    Socket fileDownloader = new Socket("localhost", 200);
                    InputStreamReader fileDownloaderStream = new InputStreamReader(fileDownloader.getInputStream());
                    while (fileDownloaderStream.ready()) {
                        System.out.print((char)fileDownloaderStream.read());
                    }
                }
*/


          /*  String query;
            while (!(query = consoleReader.readLine()).equals("exit")) {
                System.out.println("Sending \"" + query + "\"...");

                messageSender.println(query);
                messageSender.flush();

            }

            messageSender.println("exit");
            messageSender.flush();
            System.out.println("Exit command");*/
            messageSender.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
