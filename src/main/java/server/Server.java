package server;

import java.util.HashMap;
import java.util.Map;

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

    public static void main(String[] args) {

    }
}
