package common;

import java.io.PrintWriter;

/**
 * Created by Limmy on 20.01.2018.
 */
public class ClientServerUtils {
    public static void sendMessage(PrintWriter writer, String message) {
        writer.println(message);
        writer.flush();
    }
}
