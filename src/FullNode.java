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
            System.out.println(" --- Connected!");

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
                String[] parts = request.split(" "); // Separate first-line elements
                String response = "";

                switch (parts[0]) { // Request command
                    case "END" -> {
                        started = false; // Break
                        System.out.println("Connection ended.");
                    }
                    case "ECHO?" -> {
                        System.out.println("ECHO not implemented");
                    }
                    case "PUT?" -> {
                        // TODO: Implement NEAREST? check
                        StringBuilder key = new StringBuilder();
                        int keyLines = Integer.parseInt(parts[1]);
                        for (int k = 0; k < keyLines; k++) key.append(in.readLine()).append('\n');
                        StringBuilder value = new StringBuilder();
                        int valLines = Integer.parseInt(parts[2]);
                        for (int v = 0; v < valLines; v++) value.append(in.readLine()).append('\n');
                        keyValues.put(key.toString(), value.toString());
                        response = "SUCCESS";
//                        response = "FAILED";
                    }
                    case "GET?" -> {
                        StringBuilder key = new StringBuilder();
                        int keyLines = Integer.parseInt(parts[1]);
                        for (int k = 0; k < keyLines; k++) key.append(in.readLine()).append('\n');
                        String value = keyValues.get(key.toString());
                        if (value == null) response = "NOPE";
                        else {
                            String[] valParts = value.split("\n");
                            response = "VALUE " + valParts.length + '\n' + value;
                        }
                    }
                    case "NOTIFY?" -> {
                        System.out.println("NOTIFY not implemented");
                    }
                    case "NEAREST?" -> {
                        System.out.println("NEAREST not implemented");
                    }
                    default -> {
                        System.err.println("Request not recognised");
                    }
                }
                out.write(response + '\n');
                out.flush();
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
