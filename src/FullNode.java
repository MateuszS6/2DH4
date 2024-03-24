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
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean started = false;
    private HashMap<String, String> keyValues;

    public boolean listen(String ipAddress, int portNumber) {
        try {

            // Open a server socket and accept a client socket
            System.out.println("Opening server on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting for client...");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            // Create the reader and writer
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream());

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false; // The node can't accept incoming connections
        }
        return true; // The node can accept incoming connections
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	    // Implement this!
        try {

            String startRequest = reader.readLine();
            System.out.println("Received: " + startRequest);
            if (!started && startRequest.startsWith("START")) {
                System.out.println("Send: " + startRequest);
                writer.println(startRequest);
                writer.flush();
                started = true;
            }
            while (started) {
                String request = reader.readLine();
                String[] lines = request.split("\n");
                System.out.println(Arrays.toString(lines));
                String[] parts = lines[0].split(" ");
                String command = parts[0];
                if (command.equals("END")) {
                    started = false;
                } else if (command.equals("ECHO?")) {
                    // TODO
                } else if (command.equals("PUT?")) {
                    // Implement nearest check
                    int keyEnd = 1 + Integer.parseInt(parts[1]);
                    int valEnd = keyEnd + Integer.parseInt(parts[2]);
                    StringBuilder key = new StringBuilder();
                    for (int k = 1; k <= keyEnd; k++) {
                        key.append(lines[k]).append('\n');
                    }
                    StringBuilder value = new StringBuilder();
                    for (int v = keyEnd + 1; v <= valEnd; v++) {
                        value.append(lines[v]).append('\n');
                    }
                    keyValues.put(key.toString(), value.toString());
                    writer.println("SUCCESS");
                    writer.flush();
                } else if (command.equals("GET?")) {
                    // TODO
                } else if (command.equals("NOTIFY?")) {
                    // TODO
                } else if (command.equals("NEAREST?")) {
                    // TODO
                } else {
                    // TODO
                }
            }
            reader.close();
            writer.close();
            clientSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
