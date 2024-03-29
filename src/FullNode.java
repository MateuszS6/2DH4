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

            handleStart(startingNodeName);

            while (started) {
                // Read and split first line of request
                String request = readNextLine();
                System.out.println("Received: " + request);
                String[] requestParts = request.split(" "); // Separate first-line elements

                switch (requestParts[0]) { // Request command
                    case "PUT?" -> handleStore(requestParts);
                    case "GET?" -> handleGet(requestParts);
                    case "ECHO?" -> handleEcho();
                    case "NOTIFY?" -> handleNotify();
                    case "NEAREST?" -> handleNearest();
                    case "END" -> {
                        started = false; // Break
                        throw new IOException(request);
                    }
                    default -> {
                        started = false;
                        throw new IOException("Unexpected request.");
                    }
                }
            }
            clientSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void send(String string) {
        try {
            if (!string.endsWith("\n")) out.write(string + '\n');
            else out.write(string);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readNextLine() {
        String line;
        try {
            line = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return line;
    }

    public void handleStart(String startingNodeName) {
        // TODO: Robustness
        String request = readNextLine();
        System.out.println("Received: " + request);
        if (!started) {
            if (request.startsWith("START")) {
                // TODO: Implement NOTIFY?
                send("START" + 1 + startingNodeName + '\n');
                started = true;
            } else send("END Invalid START request");
        }
    }

    public void handleStore(String[] parts) {
        // TODO: Implement NEAREST? check
        StringBuilder key = new StringBuilder();
        int keyLines = Integer.parseInt(parts[1]);
        for (int k = 0; k < keyLines; k++) key.append(readNextLine()).append('\n');
        StringBuilder value = new StringBuilder();
        int valLines = Integer.parseInt(parts[2]);
        for (int v = 0; v < valLines; v++) value.append(readNextLine()).append('\n');
        keyValues.put(key.toString(), value.toString());
        send("SUCCESS");
//        send("FAILED");
    }

    public void handleGet(String[] parts) {
        StringBuilder key = new StringBuilder();
        int keyLines = Integer.parseInt(parts[1]);
        for (int k = 0; k < keyLines; k++) key.append(readNextLine()).append('\n');
        String value = keyValues.get(key.toString());
        if (value == null) send("NOPE");
        else send("VALUE " + value.split("\n").length + value);
    }

    public void handleEcho() {
        System.err.println("ECHO not implemented");
    }

    public void handleNotify() {
        System.err.println("NOTIFY? not implemented");
    }

    public void handleNearest() {
        System.err.println("NEAREST not implemented");
    }
}
