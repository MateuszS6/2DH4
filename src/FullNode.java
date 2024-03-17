// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN
// 220027090
// Mateusz.Stepien@city.ac.uk


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;

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
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	    // Implement this!

	    return;
    }
}
