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
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean started = false;
    private HashMap<String, String> keyValues;

    public boolean listen(String ipAddress, int portNumber) {
	    // Implement this!
	    // Return true if the node can accept incoming connections
	    // Return false otherwise
        try {

            System.out.println("Opening server on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting for client...");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream());

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
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
                if (request.startsWith("END ")) {
                    started = false;
                } else if (request.startsWith("PUT? ")) {

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
