// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN
// 220027090
// Mateusz.Stepien@city.ac.uk


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    private String currentFullNodeName;
    private String currentFullNodeAddress;

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
            currentFullNodeName = startingNodeName;
            currentFullNodeAddress = startingNodeAddress;

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
            return false;
        }
    }

    public boolean store(String key, String value) {
        // Send a request and check the response
        String response = Node.sendPutRequest(in, out, key, value);

        if (response.equals("FAILED")) {
            List<FullNodeInfo> visitedNodes = new ArrayList<>();
            while (response.equals("FAILED")) {
                List<FullNodeInfo> nodes = Node.sendNearestRequest(in, out, HashID.generate(key));
                boolean found = false;
                for (FullNodeInfo nodeInfo : nodes) {
                    if (!visitedNodes.contains(nodeInfo)) {
                        if (start(nodeInfo.getName(), nodeInfo.getAddress())) {
                            response = Node.sendPutRequest(in, out, key, value);
                            visitedNodes.add(nodeInfo);
                            if (response.startsWith("SUCCESS")) found = true;
                            break;
                        }
                    }
                }
                if (found) break; // Exit the loop if the value is found
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
            List<FullNodeInfo> visitedNodes = new ArrayList<>();
            while (response.startsWith("NOPE")) {
                List<FullNodeInfo> nodes = Node.sendNearestRequest(in, out, HashID.generate(key));
                boolean found = false;
                for (FullNodeInfo nodeInfo : nodes) {
                    if (!visitedNodes.contains(nodeInfo)) {
                        if (start(nodeInfo.getName(), nodeInfo.getAddress())) {
                            response = Node.sendGetRequest(in, out, key);
                            visitedNodes.add(nodeInfo);
                            if (response.startsWith("VALUE")) found = true;
                            break;
                        }
                    }
                }
                if (found) break; // Exit the loop if the value is found
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

    /*
    public String findClosestNode(String key) {
        String[] nodes = Node.sendNearestRequest(in, out, HashID.generate(key));
        String closest = nodes[0];
        if (nodes.length > 1) {
            String nodeName = closest.split(" ")[0];
            int lowestDistance = HashID.calculateDistance(key, nodeName);
            for (int i = 1; i < nodes.length; i++) {
                nodeName = nodes[i].split(" ")[0];
                int distance = HashID.calculateDistance(key, nodeName);
                if (distance < lowestDistance) {
                    lowestDistance = distance;
                    closest = nodes[i];
                }
            }
        }
        return closest;
    }
    */

    public void close(String message) {
        try {
            in.close();
            out.close();
            socket.close();
            String reason = message.split(" ")[1];
            System.out.println("Connection terminated: " + reason);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
