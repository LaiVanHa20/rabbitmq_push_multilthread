package dagoras.io.multithreadingrabbitmq.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriterExample {
    public static void main(String[] args) {
        String filename = "output.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < 10000000; i++) {
                writer.write("Đây là một dòng rất dài nè bro...\n");
            }
            System.out.println("Ghi vào file thành công!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
