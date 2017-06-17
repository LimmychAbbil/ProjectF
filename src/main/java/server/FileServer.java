package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Limmy on 17.06.2017.
 */
public class FileServer {
    private final static Logger logger = LoggerFactory.getLogger(FileServer.class);

    private class FileSender extends Thread {
        Socket socket;
        FileSender(Socket socket) {
            this.socket = socket;
        }

        public void sendAllFiles() {
//        use socket.getOutputStream();
        }
    }

    public void startFileServer() {
        try (ServerSocket fileServerSocket = new ServerSocket(200)){

            while (true) {
                Socket client = fileServerSocket.accept();

            }
        }
        catch (IOException e) {
               e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        logger.info("File Server is starting...");
        new FileServer().startFileServer();
        logger.info("File Server is started");
    }
}
