import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Node {
    public static void send(BufferedWriter writer, String string) {
        try {
            if (!string.endsWith("\n")) writer.write(string + '\n');
            else writer.write(string);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static String readNextLine(BufferedReader reader) {
        String line;
        try {
            line = reader.readLine();
            System.out.println("Received: " + line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return "END";
        }
        return line;
    }

    // Send START message
    public static String sendStartRequest(BufferedReader in, BufferedWriter out, String nodeName) {
        send(out, "START " + 1 + ' ' + nodeName);
        return readNextLine(in);
    }

    // Send END message
    public static void sendEndRequest(BufferedWriter out, String reason) {
        send(out, "END " + reason);
    }

    public static String sendEchoMessage(BufferedReader in, BufferedWriter out) {
        send(out, "ECHO");
        return readNextLine(in);
    }

    public static String sendPutRequest(BufferedReader in, BufferedWriter out, String key, String value) {
        // Split the key and value into individual words
        String[] keyParts = key.split("\n");
        if (keyParts.length < 1) System.err.println("Blank key entered.");
        String[] valueParts = value.split("\n");
        if (valueParts.length < 1) System.err.println("Blank value entered.");

        // Send PUT request
        send(out, "PUT? " + keyParts.length + ' ' + valueParts.length + '\n' + key + value);
        return readNextLine(in);
    }

    public static String sendGetRequest(BufferedReader in, BufferedWriter out, String key) {
        // Split the key into individual words
        String[] keyParts = key.split("\n");
        if (keyParts.length < 1) System.err.println("Empty key entered.");

        // Send GET request
        send(out, "GET? " + keyParts.length + '\n' + key);
        return readNextLine(in);
    }

    public static String sendNotifyRequest(BufferedReader in, BufferedWriter out, String nodeName, String nodeAddress) {
        send(out, "NOTIFY?" + '\n' + nodeName + '\n' + nodeAddress);
        return readNextLine(in);
    }

    public static List<FullNodeInfo> sendNearestRequest(BufferedReader in, BufferedWriter out, String hashID) {
        List<FullNodeInfo> nodes = null;
        Node.send(out, "NEAREST? " + hashID);
        String response = Node.readNextLine(in);

        // Check the response
        if (response.startsWith("NODES")) {
            int nodeCount = Integer.parseInt(response.split(" ")[1]);
            nodes = new ArrayList<>(nodeCount);
            for (int n = 0; n < nodeCount; n++) {
                String nodeName = readNextLine(in);
                String nodeAddress = readNextLine(in);
                nodes.add(new FullNodeInfo(nodeName, nodeAddress));
            }
            System.out.println(nodes);
        } else {
            System.err.println("Unexpected response."); // TODO: END
        }
        return nodes;
    }
}
