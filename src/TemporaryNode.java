// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Provided interface
interface TemporaryNodeInterface {
    boolean start(String startingNodeName, String startingNodeAddress);
    boolean store(String key, String value);
    String get(String key);
}

// Interface implementation
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
            // Split the address
            String[] addressParts = startingNodeAddress.split(":");

            // Initialise fields
            socket = new Socket(addressParts[0], Integer.parseInt(addressParts[1]));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Send a request and check the response
            String response = Node.sendStartRequest(in, out, startingNodeName);
            if (response.startsWith("START")) {
                System.out.println(" --- Connected!");
                return true; // 2D#4 network can be contacted
            } else {
                if (response.startsWith("END")) close(response);
                else Node.sendEndRequest(out, "Unexpected response");
                return false; // 2D#4 network can't be contacted
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean store(String key, String value) {
        // Send a request and check the response
        String response = Node.sendPutRequest(in, out, key, value);

        if (response.equals("FAILED")) {
            List<String> visitedNodeNames = new ArrayList<>();
            while (response.equals("FAILED")) {
                List<FullNodeInfo> nodes = Node.sendNearestRequest(in, out, HashID.generate(key));
                boolean worked = false;
                for (FullNodeInfo nodeInfo : nodes) {
                    if (!visitedNodeNames.contains(nodeInfo.getName())) {
                        if (start(nodeInfo.getName(), nodeInfo.getAddress())) {
                            response = Node.sendPutRequest(in, out, key, value);
                            visitedNodeNames.add(nodeInfo.getName());
                            if (response.startsWith("SUCCESS")) worked = true;
                            break;
                        }
                    }
                }
                // Exit the loop if the store worked
                if (worked) break;
            }
        }
        if (response.equals("SUCCESS")) return true; // SUCCESS -> worked
        else {
            if (response.startsWith("END")) close(response);
            else Node.sendEndRequest(out, "Unexpected response");
        }
        return false; // FAILED -> failed
    }

    public String get(String key) {
        String value = null; // Return null if the GET failed

        // Send a request and check the response
        String response = Node.sendGetRequest(in, out, key);
        if (response.startsWith("NOPE")) {
            List<String> visitedNodeNames = new ArrayList<>();
            while (response.startsWith("NOPE")) {
                List<FullNodeInfo> nodes = Node.sendNearestRequest(in, out, HashID.generate(key));
                boolean found = false;
                for (FullNodeInfo nodeInfo : nodes) {
                    if (!visitedNodeNames.contains(nodeInfo.getName()))  {
                        if (start(nodeInfo.getName(), nodeInfo.getAddress())) {
                            response = Node.sendGetRequest(in, out, key);
                            visitedNodeNames.add(nodeInfo.getName());
                            if (response.startsWith("VALUE")) found = true;
                            break;
                        }
                    }
                }
                // Exit the loop if the value is found or try limit reached
                if (found) break;
            }
        }
        if (response.startsWith("VALUE")) {
            int valueLines = Integer.parseInt(response.split(" ")[1]);
            StringBuilder valueBuilder = new StringBuilder();
            for (int v = 0; v < valueLines; v++)
                valueBuilder.append(Node.readNextLine(in)).append('\n');
            value = valueBuilder.toString();
        } else {
            if (response.startsWith("END")) close(response);
            else Node.sendEndRequest(out, "Unexpected response");
        }

        return value;
    }

    public void close(String message) {
        try {
            in.close();
            out.close();
            socket.close();
            String reason = message.split(" ")[1];
            System.out.println("Communication ended: " + reason);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
