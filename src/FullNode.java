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

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private ServerSocket serverSocket;
    private BufferedReader reader;
    private Writer writer;

    public boolean listen(String ipAddress, int portNumber) {
	    // Implement this!
	    // Return true if the node can accept incoming connections
	    // Return false otherwise
        try {

            System.out.println("Opening the server serverSocket on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server waiting for temp. node...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Temp. node connected!");

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            String message = reader.readLine();
            System.out.println("Received: " + message);
            if (message.startsWith("START")) {
                System.out.println("Responding with START to the temp. node");
                writer.write(message);
                writer.flush();
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	    // Implement this!

    }
}
