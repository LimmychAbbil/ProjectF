package client;

import common.ClientServerUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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

    private static List<Path> filesToCheck = new ArrayList<>();
    private static List<Path> filesToRedownload = new ArrayList<>();
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
        System.out.println(fileContent.toString());
        return fileContent.toString().hashCode() + "";
    }


    private static void getListOfFiles(PrintWriter messageSender, BufferedReader socketInput) throws Exception{
        ClientServerUtils.sendMessage(messageSender, "sendlistoffiles");

        while (true) {
            String answerLine = socketInput.readLine();
            if (answerLine.isEmpty()) break;
            if (answerLine.startsWith("WARNING")) filesToCheck.add(Paths.get("src/main/resources/client/examples/" + answerLine.split(":")[1]));
            if (answerLine.startsWith("CHECK")) filesToRedownload.add(Paths.get("src/main/resources/client/examples/" + answerLine.split(":")[1]));
        }
    }

    public static void connect() {
        host = "178.216.10.93";
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
                ClientServerUtils.sendMessage(messageSender, "Auth " + userName + " " + userPassword);
                Thread.sleep(200);
                String answer = socketInput.readLine();
                System.out.println(answer);
                isAuthorized = answer.equals("Success");
            }

            String summary = generateFilesCheckSummary(filesToCheck);
            ClientServerUtils.sendMessage(messageSender, "checkmywarningfiles " + summary);

            summary = generateFilesCheckSummary(filesToRedownload);
            ClientServerUtils.sendMessage(messageSender, "checkmyimportantfiles " + summary);

            while (true) {
                if (socketInput.ready()) {
                    String answer = socketInput.readLine();
                    if (answer.equals("OK")) break;
                    else {
                        System.out.println("Important files are different from servers, they will be redownload");
                        redownloadImportantFilesFromFileServer();
                        ClientServerUtils.sendMessage(messageSender, "checkmyimportantfiles " + generateFilesCheckSummary(filesToRedownload));
                    }
                }
            }

            System.out.println("Please write your messages (or 'exit')");
            String query;
            while (!(query = consoleReader.readLine()).equals("exit")) {
                System.out.println("Sending \"" + query + "\"...");

                ClientServerUtils.sendMessage(messageSender, query);

            }

            ClientServerUtils.sendMessage(messageSender, "exit");
            System.out.println("Exit command");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void redownloadImportantFilesFromFileServer() throws IOException {
        Socket fileDownloadSocket = new Socket("178.216.10.93", 200);
        Path root = Paths.get("src/main/resources/client/examples/" );
        if (!Files.exists(root)) {
            Files.createDirectories(root);

        }
        try (PrintWriter os = new PrintWriter(fileDownloadSocket.getOutputStream());
        InputStream is = fileDownloadSocket.getInputStream()){
            for (Path p : filesToRedownload) {
                Files.deleteIfExists(p);
                os.println("download " + p.getFileName());
                os.flush();
                FileOutputStream fos = new FileOutputStream("src/main/resources/client/examples/" + p.getFileName());
                System.out.println("READ FILE " + p + "FROM SERVER: ");
                while (true) {
                    if (is.available() > 0) {
                        int b = is.read();
                        if (b == 255) break;
                        System.out.print((char)b);
                        fos.write(b);
                    }
                }
                fos.close();
            }
        }
        catch (SocketException e) {e.printStackTrace();}
    }


}
