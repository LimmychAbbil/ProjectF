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
    private static List<Path> filesToCheck;
    private static List<Path> filesToReplace;
    static {
        filesToCheck = new ArrayList<>();
        filesToCheck.add(Paths.get("src/main/resources/server/examples/testFile1.txt"));
        filesToCheck.add(Paths.get("src/main/resources/server/examples/testFile2.txt"));
        logger.info("All users will be checked for editing next files:" + filesToCheck);

        filesToReplace = new ArrayList<>();
        filesToReplace.add(Paths.get("src/main/resources/server/examples/replacableFile.txt"));
        logger.info("All users will be checked for editing next files and re-download it:" + filesToReplace);
    }

    private static String filesToCheckSummary;

    private static String generateServerFilesSummary() throws IOException{
        StringBuilder filesCheckSummary = new StringBuilder();
        for (Path file: filesToCheck) {
            if (!Files.exists(file)) continue;
            BufferedReader fileReader = new BufferedReader(new FileReader(file.toFile()));
            while (fileReader.ready()) {
                filesCheckSummary.append(fileReader.readLine()).append("\n");
            }

            fileReader.close();
        }
        return filesCheckSummary.toString();
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

        private void checkFiles(BufferedReader userInput, PrintWriter serverOutput) throws IOException, InterruptedException {
            if (filesToCheck.size() == 0) return;
            StringBuilder filePathsToCheck = new StringBuilder();

            filePathsToCheck.append(filesToCheck.get(0).getFileName());
            for (int i = 1; i < filesToCheck.size(); i++) {
                filePathsToCheck.append("\n").append(filesToCheck.get(i).getFileName());
            }
            serverOutput.println(filePathsToCheck.toString());
            serverOutput.flush();

            Thread.sleep(500);

            StringBuilder clientUserSummary = new StringBuilder();
            while (userInput.ready()) {
                clientUserSummary.append(userInput.readLine()).append("\n");
            }

            if (clientUserSummary.toString().equals(filesToCheckSummary)) {
                logger.info("This user's file are OK");
            }
            else {
                logger.warn("This user's file was modified");
                System.out.println(clientUserSummary.toString() + "\n\n===\n\n " + filesToCheckSummary) ;
            }
        }

        private void rewriteEditedFiles() {

        }

        @Override
        public void run() {

            try (BufferedReader socketInput = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            PrintWriter socketOutput = new PrintWriter(userSocket.getOutputStream())){
                logger.info("Authorising user...");
                while (true) {
                    String credentials = socketInput.readLine();
                    String userName = credentials.split("_ _")[0];
                    String userPassword = credentials.split("_ _")[1];
                    if (checkAuth(userName, userPassword)) {
                        socketOutput.println("Success");
                        socketOutput.flush();
                        logger.info("User " + userName + " successfully login, start checking files");
                        checkFiles(socketInput, socketOutput);
                        break;
                    }
                    else {
                        logger.info("User " + userName + " input wrong credentials");
                        socketOutput.println("Wrong credentials");
                        socketOutput.flush();
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
        filesToCheckSummary = Server.generateServerFilesSummary();
        Server.startServer();

    }
}
