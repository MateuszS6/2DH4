// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN
// 220027090
// Mateusz.Stepien@city.ac.uk


import java.io.*;
import java.net.Socket;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public static void main(String[] args) {
        TemporaryNode client = new TemporaryNode();

        boolean start = client.start("mateusz.stepien@city.ac.uk:MyNode", "127.0.0.1:2345");
        System.out.println(start ? " -> START worked <-\n" : " -x START failed x-\n");

        boolean store = client.store("Hello\nthere\n", "General\nKenobi\n");
        System.out.println(store ? " -> STORE worked <-\n" : " -x STORE failed x-\n");

        String get = client.get("Hello\nthere\n");
        System.out.println(get != null ? " -> GET worked <-\n" : " -x GET failed x-\n");
    }

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            // Split the address and open a client socket
            String[] addressParts = startingNodeAddress.split(":");
            socket = new Socket(addressParts[0], Integer.parseInt(addressParts[1]));
            System.out.println(" --- Connected!");

            // Create the reader and writer
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Create and send a START request
            String request = "START 1 " + startingNodeName;
            Node.send(out, request);

            // Receive and check the response
            String response = Node.readNextLine(in); // START... (same as sent) -> worked
            if (!response.equals(request)) {
                if (response.equals("END")) handleEnd();
                else Node.end(out, "Unexpected response");
                return false; // 2D#4 network can't be contacted
            }
            Node.send(out, "NOTIFY?\n" + startingNodeName + '\n' + startingNodeAddress);
            if (!Node.readNextLine(in).equals("NOTIFIED")) Node.end(out, "Unexpected request");

            return true; // 2D#4 network can be contacted
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        // Split the key and value into individual words
        String[] keyParts = key.split("\n");
        if (keyParts.length < 1) System.err.println("Blank key entered.");
        String[] valueParts = value.split("\n");
        if (valueParts.length < 1) System.err.println("Blank value entered.");

//        String closestNode = findClosestNode(key);

        // Send PUT request
        Node.send(out, "PUT? " + keyParts.length + ' ' + valueParts.length + '\n' + key + value);

        // Receive and check the response
        String response = Node.readNextLine(in);
        if (!response.equals("SUCCESS")) {
            if (response.equals("FAILED")) System.err.println("Not implemented.");
            else if (response.equals("END")) handleEnd();
            else Node.end(out, "Unexpected response");
        } else return true; // SUCCESS -> worked

        return false; // FAILED -> failed
    }

    public String get(String key) {
        String value = null; // Return null if the GET failed

        // Split the key into individual words
        String[] keyParts = key.split("\n");
        if (keyParts.length < 1) System.err.println("Empty key entered.");

        // Send GET request
        Node.send(out, "GET? " + keyParts.length + '\n' + key);

        // Receive and check the response
        String response = Node.readNextLine(in);
        if (!response.startsWith("VALUE")) {
            if (response.equals("NOPE")) System.err.println("Not implemented."); // TODO: Find next closest nodes
            else if (response.equals("END")) handleEnd();
            else Node.end(out, "Unexpected response");
        } else {
            int valueLines = Integer.parseInt(response.split(" ")[1]);
            StringBuilder valueBuilder = new StringBuilder();
            for (int v = 0; v < valueLines; v++) valueBuilder.append(Node.readNextLine(in)).append('\n');
            value = valueBuilder.toString();
        }

        return value;
    }

    public void handleEnd() {
        try {
            in.close();
            out.close();
            socket.close();
            System.err.println("Connection terminated.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String findClosestNode(String string) {
        String[] nodes = Node.nearest(in, out, HashID.generate(string));
        String closest = nodes[0];
        if (nodes.length > 1) {
            String nodeName = closest.split(" ")[0];
            int lowestDistance = HashID.calculateDistance(string, nodeName);
            for (int i = 1; i < nodes.length; i++) {
                nodeName = nodes[i].split(" ")[0];
                int distance = HashID.calculateDistance(string, nodeName);
                if (distance < lowestDistance) {
                    lowestDistance = distance;
                    closest = nodes[i];
                }
            }
        }
        return closest;
    }
}
