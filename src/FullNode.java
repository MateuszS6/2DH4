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
import java.util.HashMap;
import java.util.TreeMap;

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
    private boolean started = false;
    private HashMap<Integer, String[]> network = new HashMap<>();
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
	    // Implement this!
        try {
            clientSocket = serverSocket.accept();
            System.out.println(" --- Connected!");

            // Create the reader and writer
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            if (!started) handleStart(startingNodeName);
            while (started) {
                // Read and split first line of request
                String request = Node.readNextLine(in);
                String[] requestParts = request.split(" ");
                String requestWord = requestParts[0]; // Request command

                if (requestWord.equals("PUT?")) handleStore(requestParts, startingNodeName);
                else if (requestWord.equals("GET?")) handleGet(requestParts);
                else if (requestWord.equals("ECHO?")) handleEcho();
                else if (requestWord.equals("NOTIFY?")) handleNotify();
                else if (requestWord.equals("NEAREST?")) handleNearest();
                else if (requestWord.equals("END")) handleEnd(request.substring(4));
                else handleEnd("Unexpected request.");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void handleStart(String name) {
        // TODO: Robustness
        if (Node.readNextLine(in).startsWith("START")) {
            // TODO: Implement NOTIFY?
            Node.send(out, "START 1 " + name);
            started = true;
        } else Node.send(out, "END Invalid START request");
    }

    public void handleStore(String[] parts, String name) {
        // TODO: Implement NEAREST? check
        StringBuilder key = new StringBuilder();
        int keyLines = Integer.parseInt(parts[1]);
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');
//        String keyHashID = HashID.bytesToHex(HashID.computeHashID(key.toString()));
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

    public void handleNotify() {
        System.err.println("NOTIFY? not implemented");
    }

    public void handleNearest() {
        System.err.println("NEAREST not implemented");
    }

    public void handleEnd(String reason) {
        try {
            started = false; // Break
            clientSocket.close();
            serverSocket.close();
            System.err.println("Connection terminated.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
