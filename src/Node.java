import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Node {
    public static void send(BufferedWriter writer, String string) {
        try {
            if (!string.endsWith("\n")) writer.write(string + '\n');
            else writer.write(string);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readNextLine(BufferedReader reader) {
        String line;
        try {
            line = reader.readLine();
            if (line.isEmpty()) line = "END";
            System.out.println("Received: " + line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return line;
    }
}
