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
import java.util.Arrays;
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
    private HashMap<String, String> keyValues = new HashMap<>();

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
            System.out.println("Client connected!");

            // Create the reader and writer
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String startRequest = in.readLine();
            System.out.println("Received: " + startRequest);
            if (!started && startRequest.startsWith("START")) {
                out.write("START 1 " + startingNodeName + '\n');
                out.flush();
//                System.out.println("Sending: " + startRequest);
                started = true;
            }
            while (started) {
                String request = in.readLine();
                System.out.println("Received: " + request);
                String[] lines = request.split("\n"); // Separate lines
                String[] parts = lines[0].split(" "); // Separate first-line elements
                switch (parts[0]) { // Request word
                    case "END" -> {
                        started = false;
                        System.out.println("Connection ended.");
                    }
                    case "ECHO?" -> {
                        System.out.println("Echo back");
                        // TODO
                    }
                    case "PUT?" -> {
                        // Implement nearest check
//                        int keyEnd = 1 + Integer.parseInt(parts[1]);
//                        int valEnd = keyEnd + Integer.parseInt(parts[2]);
//                        StringBuilder key = new StringBuilder();
//                        for (int k = 1; k <= keyEnd; k++) {
//                            key.append(lines[k]).append('\n');
//                        }
//                        StringBuilder value = new StringBuilder();
//                        for (int v = keyEnd + 1; v <= valEnd; v++) {
//                            value.append(lines[v]).append('\n');
//                        }
//                        keyValues.put(key.toString(), value.toString());
//                        writer.write("SUCCESS");
//                        writer.flush();
                    }
                    case "GET?" -> {
                        System.out.println(command + " not implemented");
                    }
                    case "NOTIFY?" -> {
                        System.out.println(command + " not implemented");
                    }
                    case "NEAREST?" -> {
                        System.out.println(command + " not implemented");
                    }
                    default -> {
                        System.err.println("Request not recognised");
                    }
                }
            }
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
