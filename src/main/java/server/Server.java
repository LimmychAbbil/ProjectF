package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.users.User;
import server.users.UserGroup;

/**
 * Created by Limmy on 27.05.2017.
 */
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    static {
        BasicConfigurator.configure();
    }
    private static List<Path> filesWithAlert;
    private static List<Path> filesToReplace;
    static {
        filesWithAlert = new ArrayList<>();
        filesWithAlert.add(Paths.get("src/main/resources/server/examples/testFile1.txt"));
        filesWithAlert.add(Paths.get("src/main/resources/server/examples/testFile2.txt"));
        logger.info("All users will be checked for editing next files:" + filesWithAlert);

        filesToReplace = new ArrayList<>();
        filesToReplace.add(Paths.get("src/main/resources/server/examples/replacableFile.txt"));
        logger.info("All users will be checked for editing next files and re-download it:" + filesToReplace);
    }

    private static String filesToCheckSummary;
    private static String filesToReplaceSummary;

    private static String generateServerFilesSummary(List<Path> filesToCheck) throws IOException{
        StringBuilder filesCheckSummary = new StringBuilder();
        for (Path file: filesToCheck) {
            if (!Files.exists(file)) continue;
            FileReader fileReader = new FileReader(file.toFile());
            while (fileReader.ready()) {
                filesCheckSummary.append((char)fileReader.read());
            }

            fileReader.close();
        }
        return filesCheckSummary.toString().hashCode() + "";
    }

    private static Set<User> users;
    static {
        users = new HashSet<>();
        users.add(new User(UserGroup.ADMIN,"admin", "admin"));
        users.add(new User("User1", "1"));
        users.add(new User("User2", "2"));
    }

    private static class UserHandler extends Thread {
        private Socket userSocket;

        public UserHandler(Socket userSocket) {
            this.userSocket = userSocket;
        }

        private boolean checkAuth(String userName, String userPas) {
            boolean existingUser = false;
            boolean correctPass = false;
            for (User u: users) {
                if (userName.equalsIgnoreCase(u.getUserName())) {
                    existingUser = true;
                    correctPass = u.checkPassword(userPas);
                    break;
                }
            }

            return existingUser && correctPass;
        }

        private void checkFiles(String clientFilesSummary) throws IOException, InterruptedException {
            if (clientFilesSummary.toString().equals(filesToCheckSummary)) {
                logger.info("This user's file are OK");
            }
            else {
                logger.warn("This user's file was modified:\n{}", clientFilesSummary.toString() + "\n=====\n" + filesToCheckSummary);
            }
        }

        private boolean checkRewritableFiles (String clientFilesSummary, PrintWriter serverOutput) throws IOException {
            if (clientFilesSummary.toString().equals(filesToReplaceSummary)) {
                logger.info("This user's important file are OK");
                serverOutput.println("OK");
                serverOutput.flush();
                return true;
            }
            else {
                logger.warn("This user's important file was modified " +
                        "and should be redownload:\n{}", clientFilesSummary.toString() + "\n=====\n" + filesToCheckSummary);
                serverOutput.println("Redownload files");
                serverOutput.flush();
                return false;
            }
        }


        @Override
        public void run() {

            try (BufferedReader socketInput = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            PrintWriter socketOutput = new PrintWriter(userSocket.getOutputStream())){
                logger.info("Authorising user first...");
                while (true) {
                    String command = socketInput.readLine();
                    if (command.toLowerCase().startsWith("auth")) {
                        String userName = command.split(" ")[1];
                        String userPassword = command.split(" ")[2];
                        if (checkAuth(userName, userPassword)) {
                            socketOutput.println("Success");
                            socketOutput.flush();
                            logger.info("User " + userName + " successfully login, start checking files");
                            break;
                        } else {
                            logger.info("User " + userName + " input wrong credentials");
                            socketOutput.println("Wrong credentials");
                            socketOutput.flush();
                        }
                    }
                    else if (command.equals("sendlistoffiles")) {
                        sendListOfFiles(socketOutput);
                    }
                    else {
                        socketOutput.println("Authorize first");
                        socketOutput.flush();
                    }
                }

                while (true) {
                    if (socketInput.ready()) {
                        String command = socketInput.readLine();
                        if (command.toLowerCase().startsWith("checkmywarningfiles")) {
                            String userSummary = command.split(" ")[1];
                            checkFiles(userSummary);
                        }
                        else if (command.toLowerCase().startsWith("checkmyimportantfiles")) {
                            String userSummary = command.split(" ")[1];
                            if (checkRewritableFiles(userSummary, socketOutput)) break;
                        }
                    }
                }
                logger.info("Start processing user command");

                while (true) {
                    if (socketInput.ready()) {
                        String inputString = socketInput.readLine();
                        logger.info(inputString);
                        if (inputString.equalsIgnoreCase("exit")) break;
                    }
                }
                logger.info("User disconnected");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendListOfFiles(PrintWriter socketOutput) {
            if (filesWithAlert.size() == 0) {
                socketOutput.println();
                socketOutput.flush();
            }
            else {
                for (Path p: filesWithAlert) {
                    socketOutput.println("WARNING:" + p.getFileName());
                }
                for (Path p: filesToReplace) {
                    socketOutput.println("CHECK:" + p.getFileName());
                }
                socketOutput.println("");
                socketOutput.flush();
            }
        }
    }

    public static void startServer() throws IOException {
        final ServerSocket socket = new ServerSocket(31);
        logger.info("Server start");
        while (true) {
            final Socket userSocket = socket.accept();
            logger.info("Somebody have just connected. Ip " + userSocket.getInetAddress().toString());
            UserHandler userHandler = new UserHandler(userSocket);
            userHandler.setDaemon(true);
            userHandler.start();
        }
    }

    public static void main(String[] args) throws Exception {
        filesToCheckSummary = Server.generateServerFilesSummary(filesWithAlert);
        filesToReplaceSummary = Server.generateServerFilesSummary(filesToReplace);
        Server.startServer();

    }
}
