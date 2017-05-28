package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    static {
        users = new HashMap<>();
        users.put("Admin", "admin");
        users.put("User1", "1");
        users.put("User2", "2");
        users.put("User3", "3");
    }


    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger logger = LoggerFactory.getLogger(Server.class);
        ServerSocket socket = new ServerSocket(31);
        logger.warn("Server start");


        while (true) {
            Socket userSocket = socket.accept();
            logger.warn("Somebody have just connected. Ip " + userSocket.getInetAddress().toString());
        }

    }
}
