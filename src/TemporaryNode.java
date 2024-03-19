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
        try {

            // Split the address and initialise the socket, reader, and writer
            String[] addressParts = startingNodeAddress.split(":");
            clientSocket = new Socket(addressParts[0], Integer.parseInt(addressParts[1]));
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Create and send a START message
            String message = "START 1" + startingNodeName;
            writer.write(message);
            writer.flush();

            // Receive and check the response
            String response = reader.readLine();
            if (!response.startsWith("START")) {
                clientSocket.close();
                return false; // 2D#4 network can't be contacted
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true; // 2D#4 network can be contacted
    }

    public boolean store(String key, String value) {
        try {

            // Count the lines in the key and value, and send a PUT request
            long keyLines = key.chars().filter(ch -> ch == '\n').count();
            long valLines = value.chars().filter(ch -> ch == '\n').count();
            String request = "PUT? " + keyLines + ' ' + valLines + '\n'
                    + key + '\n'
                    + value;
            writer.write(request);
            writer.flush();

            // Receive and check the response
            String response = reader.readLine();
            return response.equals("SUCCESS"); // SUCCESS -> worked; FAILED -> failed

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false; // The STORE failed
        }
    }

    public String get(String key) {
        String value = null; // Return null if the GET failed
        try {

            // Count the lines in the key, and send a GET request for the value
            long keyLines = key.chars().filter(ch -> ch == '\n').count();
            String request = "GET? " + keyLines + '\n' + key;
            writer.write(request);
            writer.flush();

            // Receive and check the response
            String response = reader.readLine();
            if (response.startsWith("VALUE")) // VALUE... -> worked; NOPE -> failed
                value = response.substring(response.indexOf('\n') + 1); // The string value is a substring

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return value; // The GET worked/failed
    }
}
