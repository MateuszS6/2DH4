// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN
// 220027090
// Mateusz.Stepien@city.ac.uk


import java.io.*;
import java.net.Socket;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    private BufferedReader reader;
    private Writer writer;
    private Socket clientSocket;
    public boolean start(String startingNodeName, String startingNodeAddress) {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted

        String[] addressParts = startingNodeAddress.split(":");
        try {
            clientSocket = new Socket(addressParts[0], Integer.parseInt(addressParts[1]));
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            String startMsg = "START 1" + startingNodeName + "\n";
            writer.write(startMsg);
            writer.flush();

            String rsp = reader.readLine();
            if (!rsp.contains("START")) {
                clientSocket.close();
                return false;
            }
            return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
	    // Implement this!
	    // Return true if the store worked
	    // Return false if the store failed
	    return true;
    }

    public String get(String key) {
	    // Implement this!
	    // Return the string if the get worked
	    // Return null if it didn't
	    return "Not implemented";
    }
}
