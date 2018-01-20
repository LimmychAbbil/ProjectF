package client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by Limmy on 18.06.2017.
 */
public class TestFileClient {
    public static void main(String[] args) throws Exception{
        File f1 = new File("src/main/resources/client/examples/testFile1.txt");
        System.out.println(f1.exists());
        FileReader fr1 = new FileReader(f1);
        StringBuilder sb1 = new StringBuilder();
        while (fr1.ready()) {
            sb1.append((char)fr1.read());
        }
        System.out.println(sb1.toString().hashCode());
        fr1.close();
        File f2 = new File("src/main/resources/server/examples/testFile1.txt");
        System.out.println(f2.exists());
        FileReader fr2 = new FileReader(f2);
        StringBuilder sb2 = new StringBuilder();
        while (fr2.ready()) {
            sb2.append((char)fr2.read());
        }
        System.out.println(sb2.toString().hashCode());


        System.out.println(sb1.toString());
        System.out.println(sb2.toString());
    }
}
