// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN
// 220027090
// Mateusz.Stepien@city.ac.uk


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String name;
    private String address;
    private boolean started = false;
    private HashMap<Integer, List<String>> network = new HashMap<>();
    private HashMap<String, String> keyValues = new HashMap<>();

    public static void main(String[] args) {
        FullNode server = new FullNode();
        if (server.listen("127.0.0.1", 2345))
            server.handleIncomingConnections("mateusz.stepien@city.ac.uk:MyNode", "127.0.0.1:2345");
    }

    public boolean listen(String ipAddress, int portNumber) {
        try {
            // Open a server socket and accept a client socket
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting for client...");

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false; // The node can't accept incoming connections
        }
        return true; // The node can accept incoming connections
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        name = startingNodeName;
        address = startingNodeAddress;
        for (int d = 0; d < 257; d++) network.put(d, new ArrayList<>(3));
        addToNetwork(name, address);
        System.out.println(network);

        try {
            clientSocket = serverSocket.accept();
            System.out.println(" --- Connected!");

            // Create the reader and writer
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            if (!started) {
                handleStart();
                notifyInfo();
            }
            while (started) {
                // Read and split first line of request
                String request = Node.readNextLine(in);
                String[] requestParts = request.split(" ");
                String command = requestParts[0];

                if (command.equals("PUT?")) handlePut(requestParts);
                else if (command.equals("GET?")) handleGet(requestParts);
                else if (command.equals("ECHO?")) handleEcho();
                else if (command.equals("NOTIFY?")) handleNotify();
                else if (command.equals("NEAREST?")) handleNearest(requestParts[2]);
                else if (command.equals("END")) handleEnd();
                else Node.end(out, "Unexpected request");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void addToNetwork(String nodeName, String nodeAddress) {
        int distance = HashID.calculateDistance(name, nodeName);
        network.get(distance).add(nodeName + ' ' + nodeAddress);
    }

    public void handleStart() {
        // TODO: Robustness
        if (Node.readNextLine(in).startsWith("START")) {
            Node.send(out, "START 1 " + name);
            started = true;
        } else Node.end(out, "Unexpected request");
    }

    public void handlePut(String[] parts) {
        // TODO: Implement NEAREST? check
        StringBuilder key = new StringBuilder();
        int keyLines = Integer.parseInt(parts[1]);
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');
        String keyHashID = HashID.generate(key.toString());
        System.out.println(keyHashID);
        handleNearest(keyHashID);
        // Loop through network map
        // Compute hash ID of each node name, comparing to key hash ID
        // Return 3 closest
        // If this node is one of those, store
        StringBuilder value = new StringBuilder();
        int valueLines = Integer.parseInt(parts[2]);
        for (int v = 0; v < valueLines; v++) value.append(Node.readNextLine(in)).append('\n');
        keyValues.put(key.toString(), value.toString());
        Node.send(out, "SUCCESS");
//        Node.send(out, "FAILED");
    }

    public void handleGet(String[] parts) {
        int keyLines = Integer.parseInt(parts[1]);
        StringBuilder key = new StringBuilder();
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');
        String value = keyValues.get(key.toString());
        if (value == null) Node.send(out, "NOPE");
        else Node.send(out, "VALUE " + value.split("\n").length + '\n' + value);
    }

    public void handleEcho() {
        Node.send(out, "OHCE");
    }

    public void notifyInfo() {
        Node.send(out, "NOTIFY?\n" + name + '\n' + address);
        if (!Node.readNextLine(in).equals("NOTIFIED")) Node.end(out, "Unexpected request");
    }

    public void handleNotify() {
        String nodeName = Node.readNextLine(in);
        String nodeAddress = Node.readNextLine(in);
        addToNetwork(nodeName, nodeAddress);
        Node.send(out, "NOTIFIED");
    }

    public void handleNearest(String hashID) {
        // TODO: Implement
//        for (Map.Entry<Integer, List<String>> entry : network.entrySet()) {
//            for (String node : entry.getValue()) {
//
//            }
//        }
        int nodeInfoLines = 3;
        String node1 = "martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-1\n10.0.0.4:2244";
        String node2 = "martin.brain@city.ac.uk:MyCoolImplementation,1.67,test-node-7\n10.0.0.23:2400";
        String node3 = "martin.brain@city.ac.uk:MyCoolImplementation,1.67,test-node-9\n10.0.0.96:35035";
        Node.send(out, "NODES " + nodeInfoLines + '\n' + node1  + '\n' + node2 + '\n' + node3);
    }

    public void handleEnd() {
        try {
            started = false; // Break
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
            System.err.println("Connection terminated.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
