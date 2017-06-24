package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
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

            String summary = generateFilesCheckSummary(filesWithAlert);
            messageSender.println("checkmywarningfiles " + summary);
            messageSender.flush();

            summary = generateFilesCheckSummary(filesToReplace);
            messageSender.println("checkmyimportantfiles " + summary);
            messageSender.flush();

            while (true) {
                if (socketInput.ready()) {
                    String answer = socketInput.readLine();
                    if (answer.equals("OK")) break;
                    else {
                        System.out.println("Important files are different from servers, they will be redownload");
                        redownloadImportantFilesFromFileServer();
                        messageSender.println("checkmyimportantfiles " + generateFilesCheckSummary(filesToReplace));
                        messageSender.flush();
                    }
                }
            }

            System.out.println("Please write your messages (or 'exit')");
            String query;
            while (!(query = consoleReader.readLine()).equals("exit")) {
                System.out.println("Sending \"" + query + "\"...");

                messageSender.println(query);
                messageSender.flush();

            }

            messageSender.println("exit");
            messageSender.flush();
            System.out.println("Exit command");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void redownloadImportantFilesFromFileServer() throws IOException {
        Socket fileDownloadSocket = new Socket("localhost", 200);
        try (PrintWriter os = new PrintWriter(fileDownloadSocket.getOutputStream());
        InputStream is = fileDownloadSocket.getInputStream()){
            for (Path p : filesToReplace) {
                Files.deleteIfExists(p);
                os.println("download " + p.getFileName());
                os.flush();
                FileOutputStream fos = new FileOutputStream("src/main/resources/client/examples/" + p.getFileName());
                while (true) {
                    if (is.available() > 0) {
                        int b = is.read();
                        if (b == 255) break;
                        fos.write(b);
                    }
                }
                fos.close();
            }
        }
        catch (SocketException e) {e.printStackTrace();}
    }


}
