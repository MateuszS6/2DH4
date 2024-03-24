// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// MATEUSZ STEPIEN
// 220027090
// Mateusz.Stepien@city.ac.uk


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {

            // Split the address and create the serverSocket
            String[] addressParts = startingNodeAddress.split(":");
            String ipAddress = addressParts[0];
            int portNumber = Integer.parseInt(addressParts[1]);
            System.out.println("Connecting to " + ipAddress + ':' + portNumber);
            socket = new Socket(ipAddress, portNumber);
            System.out.println("Connected to server!");

            // Create the reader and writer
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            // Create and send a START message
            String message = "START 1 " + startingNodeName;
            System.out.println("Sending: " + message);
            writer.println(message);
            writer.flush();

            // Receive and check the response
            String response = reader.readLine();
            System.out.println("Received: " + response);
            if (response.equals(message)) {
                System.out.println("Start successful!");
                return true; // 2D#4 network can be contacted
            } else {
                socket.close();
                return false; // 2D#4 network can't be contacted
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        try {

            // Count the lines in the key and value, and send a PUT request
            long keyLines = key.chars().filter(ch -> ch == '\n').count();
            long valLines = value.chars().filter(ch -> ch == '\n').count();
            String request = "PUT? " + keyLines + ' ' + valLines + '\n'
                    + key + '\n'
                    + value;
            writer.println(request);
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
            writer.println(request);
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
