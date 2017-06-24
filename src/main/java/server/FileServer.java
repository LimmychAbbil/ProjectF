package server;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Limmy on 17.06.2017.
 */
public class FileServer {
    private final static Logger logger = LoggerFactory.getLogger(FileServer.class);
    static {
        BasicConfigurator.configure();
    }

    private class FileSender extends Thread {
        Socket socket;
        FileSender(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream os = socket.getOutputStream()){
                while (true) {
                    if (is.ready()) {
                        String command = is.readLine();
                        if (command.equals("exit")) break;
                        else if (command.toLowerCase().startsWith("download")) {
                            String fileName = command.split(" ")[1];
                            logger.info("Send " + fileName + " to client");
                            sendFile(os, fileName);
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFile(OutputStream os, String fileName) throws IOException {
            FileInputStream fis = new FileInputStream("src/main/resources/server/examples/" + fileName);
            while (fis.available() > 0) {
                os.write(fis.read());
            }
            os.write(-1);
            fis.close();
        }
    }

    public void startFileServer() {
        try (ServerSocket fileServerSocket = new ServerSocket(200)){

            while (true) {
                Socket client = fileServerSocket.accept();
                new FileSender(client).start();
            }
        }
        catch (IOException e) {
               e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        logger.info("File Server is starting...");
        new FileServer().startFileServer();
    }
}
