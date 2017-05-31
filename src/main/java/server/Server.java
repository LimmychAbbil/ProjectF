package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Limmy on 27.05.2017.
 */
public class Server {
    private static Map<String, String> users;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    static {
        users = new HashMap<>();
        users.put("Admin", "admin");
        users.put("User1", "1");
        users.put("User2", "2");
        users.put("User3", "3");
    }

    public void startServer() throws IOException {
        final ServerSocket socket = new ServerSocket(31);
        logger.info("Server start");
        while (true) {
            final Socket userSocket = socket.accept();
            logger.info("Somebody have just connected. Ip " + userSocket.getInetAddress().toString());
            new Thread() {
                @Override
                public void run() {
                    try (BufferedReader socketInput = new BufferedReader(new InputStreamReader(userSocket.getInputStream()))){
                        logger.info("Authorising user...");
                        while (!socketInput.ready()) {
                        }
                        String userName = socketInput.readLine();
                        logger.info(userName);
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
            }.start();
        }
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        new Server().startServer();

    }
}
