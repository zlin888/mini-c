package util;

import java.io.FileWriter;

public class ToFile {
    public static void writeTo(String path, String content) {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(content);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
