package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.users.User;
import server.users.UserGroup;

/**
 * Created by Limmy on 27.05.2017.
 */
public class Server {
    private static Set<User> users;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    static {
        users = new HashSet<>();
        users.add(new User(UserGroup.ADMIN,"admin", "admin"));
        users.add(new User("User1", "1"));
        users.add(new User("User2", "2"));
    }

    private class UserHandler extends Thread {
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
                        logger.info("User " + userName + " successfully login");
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
            } catch (IOException e) {
            }
        }
    }

    public void startServer() throws IOException {
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
        BasicConfigurator.configure();
        new Server().startServer();

    }
}
