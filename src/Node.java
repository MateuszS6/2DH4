import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

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

    public static String[] nearest(BufferedReader reader, BufferedWriter writer, String hashID) {
        String[] nodes = new String[3];
        Node.send(writer, "NEAREST? " + hashID);
        String response = Node.readNextLine(reader);
        if (!response.startsWith("NODES")) System.err.println("Unexpected response."); // TODO: END
        else {
            int number = Integer.parseInt(response.split(" ")[1]);
            for (int n = 0; n < number; n++) {
                nodes[n] = Node.readNextLine(reader);
                nodes[n] += ' ' + Node.readNextLine(reader);
            }
            System.out.println(Arrays.toString(nodes));
        }
        return nodes;
    }

    public static void end(BufferedWriter writer, String reason) {
        send(writer, "END " + reason);
    }
}
