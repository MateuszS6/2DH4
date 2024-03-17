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
    private Socket clientSocket;
    private BufferedReader reader;
    private Writer writer;
    public boolean start(String startingNodeName, String startingNodeAddress) {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted
        try {

            String[] addressParts = startingNodeAddress.split(":");
            clientSocket = new Socket(addressParts[0], Integer.parseInt(addressParts[1]));
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            String message = "START 1" + startingNodeName;
            writer.write(message);
            writer.flush();

            String response = reader.readLine();
            if (!response.startsWith("START")) {
                clientSocket.close();
                reader.close();
                writer.close();
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
        try {

            long keyLines = key.chars().filter(ch -> ch == '\n').count();
            long valLines = value.chars().filter(ch -> ch == '\n').count();
            String request = "PUT? " + keyLines + ' ' + valLines + '\n'
                    + key + '\n'
                    + value;
            writer.write(request);
            writer.flush();

            String response = reader.readLine();
            return response.equals("SUCCESS");

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public String get(String key) {
	    // Implement this!
	    // Return the string if the get worked
	    // Return null if it didn't
        String value = null;
        try {

            long keyLines = key.chars().filter(ch -> ch == '\n').count();
            String request = "GET? " + keyLines + '\n' + key;
            writer.write(request);
            writer.flush();

            String response = reader.readLine();
            if (response.startsWith("VALUE"))
                value = response.substring(response.indexOf('\n') + 1);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return value;
    }
}
