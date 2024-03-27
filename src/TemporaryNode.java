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
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {

            // Split the address and open a client socket
            String[] addressParts = startingNodeAddress.split(":");
            socket = new Socket(addressParts[0], Integer.parseInt(addressParts[1]));
            System.out.println(" --- Connected!");

            // Create the reader and writer
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Create and send a START request
            String request = "START 1 " + startingNodeName;
//            System.out.println("Sending: " + request);
            out.write(request + '\n');
            out.flush();

            // Receive and check the response
            String response = in.readLine();
            System.out.println("Received: " + response);
            if (response.equals(request)) { // START... (same as sent) -> worked
                System.out.println("Start successful!");
                return true; // 2D#4 network can be contacted
            } else {
                socket.close();
                throw new IOException("START unsuccessful");
            }

//            return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false; // 2D#4 network can't be contacted
        }
    }

    public boolean store(String key, String value) {
        try {

            // Split the key and value into individual words
            String[] keyParts = key.split("\n");
            if (keyParts.length < 1) throw new IOException("Blank key entered");
            String[] valueParts = value.split("\n");
            if (valueParts.length < 1) throw new IOException("Blank value entered");

            // Format and send the PUT request with new lines for each word
            StringBuilder request = new StringBuilder("PUT? " + keyParts.length + ' ' + valueParts.length + '\n');
            for (String k : keyParts) request.append(k).append('\n');
            for (String v : valueParts) request.append(v).append('\n');
            out.write(request.toString());
            out.flush();

            // Receive and check the response
            String response = in.readLine();
            System.out.println("Received: " + response);
            if (response.equals("SUCCESS")) return true; // SUCCESS -> worked
            else if (response.equals("FAILED")) return false; // FAILED -> failed
            else throw new IOException("Unexpected response: " + response);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false; // The STORE failed
        }
    }

    public String get(String key) {
        String value = null; // Return null if the GET failed
        try {

            // Split the key into individual words
            String[] keyParts = key.split("\n");
            if (keyParts.length < 1) throw new IOException("Empty key entered");

            // Format and send the GET request
            StringBuilder request = new StringBuilder("GET? " + keyParts.length + '\n');
            for (String k : keyParts) request.append(k).append('\n');
            out.write(request.toString() + '\n');
            out.flush();

            // Receive and check the response
            String response = in.readLine();
            System.out.println("Received: " + response);
            if (response.startsWith("VALUE")) { // VALUE... -> worked
                String[] responseParts = response.split(" ");
                int valLines = Integer.parseInt(responseParts[1]);
                StringBuilder getVal = new StringBuilder();
                for (int v = 0; v < valLines; v++) getVal.append(in.readLine()).append('\n');
                value = getVal.toString();
            }
            else if (response.equals("NOPE")) System.out.println(response); // NOPE -> failed
            else throw new IOException("Unexpected response: " + response);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return value; // The GET worked/failed
    }
}
