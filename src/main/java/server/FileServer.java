package server;

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

    private class FileSender extends Thread {
        Socket socket;
        FileSender(Socket socket) {
            this.socket = socket;
        }

        public void sendAllFiles(List<Path> filesToSend) throws IOException {
//        use socket.getOutputStream();
            OutputStream fileWriter = socket.getOutputStream();
            for (int i = 0; i < filesToSend.size(); i++) {
                fileWriter.write(filesToSend.get(i).getFileName().toString().getBytes());
                FileInputStream inputStream = new FileInputStream(filesToSend.get(i).toFile());
                byte[] buff = new byte[(int) Files.size(filesToSend.get(0))];
                inputStream.read(buff);
                fileWriter.write(buff);
                fileWriter.flush();
            }
            fileWriter.close();
        }
    }

    public void startFileServer() {
        try (ServerSocket fileServerSocket = new ServerSocket(200)){

            while (true) {
                Socket client = fileServerSocket.accept();
                List<Path> stubList = new ArrayList<>();
                stubList.add(Paths.get("src/main/resources/server/examples/replacableFile.txt"));
                new FileSender(client).sendAllFiles(stubList);
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
