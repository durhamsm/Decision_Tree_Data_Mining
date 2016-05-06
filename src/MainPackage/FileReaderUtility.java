package MainPackage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileReaderUtility {

    public static BufferedReader getBufferedReader(String fileName) {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return buf;
    }

}
