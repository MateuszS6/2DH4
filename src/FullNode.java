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
import java.util.*;

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
    private FullNodeInfo info;
    private String connectedNodeAddress;
    private boolean communicationStarted = false;
    private final Map<Integer, List<FullNodeInfo>> networkMap = new HashMap<>();
    private final Map<String, String> keyValues = new HashMap<>();

    public static void main(String[] args) {
        FullNode server = new FullNode();
        if (server.listen("127.0.0.1", 2345))
            server.handleIncomingConnections("mateusz.stepien@city.ac.uk:MyNode", "127.0.0.1:2345");
    }

    public boolean listen(String ipAddress, int portNumber) {
        try {
            // Open a server socket and accept a client socket
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting for client...");
            return true; // The node can accept incoming connections
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false; // The node can't accept incoming connections
        }
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        setupNodeInfo(startingNodeName, startingNodeAddress);
        prepareNetworkMap();

        // Accept client and initialise communication streams
        try {
            clientSocket = serverSocket.accept();
            System.out.println(" --- Client connected!");
            initialiseCommunicationStreams();

            // Process requests from connected client
            if (!communicationStarted) handleStart();
            while (communicationStarted) {
                // Read and split first line of request
                String request = Node.readNextLine(in);
                if (request == null) {
                    disconnectCurrentNode(null);
                    break;
                }

                // Get the parts of the request
                String[] requestParts = request.split(" ");

                if (request.startsWith("PUT?") && requestParts.length == 3) handlePut(requestParts);
                else if (request.startsWith("GET?") && requestParts.length == 2) handleGet(requestParts);
                else if (request.startsWith("ECHO?") && requestParts.length == 1) handleEcho();
                else if (request.startsWith("NOTIFY?") && requestParts.length == 1) handleNotify();
                else if (request.startsWith("NEAREST?") && requestParts.length == 2) handleNearest(requestParts);
                else if (request.startsWith("END") && requestParts.length == 2) close();
                else disconnectCurrentNode(request);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void initialiseCommunicationStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    }

    private void setupNodeInfo(String name, String address) {
        info = new FullNodeInfo(name, address);
        connectedNodeAddress = address;
    }

    private void prepareNetworkMap() {
        for (int d = 0; d < 257; d++) networkMap.put(d, new ArrayList<>(3));
        addToNetworkMap(info);
    }

    private void addToNetworkMap(FullNodeInfo newNodeInfo) {
        int distance = HashID.calculateDistance(info.getAddress(), newNodeInfo.getAddress());
        if (networkMap.get(distance).size() > 2) System.err.println("Max capacity reached at distance, adjusting...");
        while (networkMap.get(distance).size() > 2) networkMap.get(distance).remove(0);
        networkMap.get(distance).add(newNodeInfo);
        System.out.println("Added " + newNodeInfo.getName() + " at distance " + distance);
    }

    private void addToNetworkMap(String nodeName, String nodeAddress) {
        addToNetworkMap(new FullNodeInfo(nodeName, nodeAddress));
    }

    private void removeFromNetworkMap(String nodeAddress) {
        boolean found = false;
        for (Map.Entry<Integer, List<FullNodeInfo>> entry : networkMap.entrySet()) {
            for (FullNodeInfo nodeInfo : entry.getValue())
                if (nodeInfo.getAddress().equals(nodeAddress)) {
                    entry.getValue().remove(nodeInfo);
                    found = true;
                    break;
                }
            if (found) return;
        }
        System.err.println("Node to remove not found in network map");
    }

    private boolean notifyCurrentNode() {
        String response = Node.sendNotifyRequest(in, out, info.getName(), info.getAddress());
        return response.equals("NOTIFIED");
    }

    private void handleStart() {
        String message = Node.readNextLine(in);
        if (message != null && message.startsWith("START")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) if (parts[2].contains(":")) {
                Node.send(out, "START " + 1 + ' ' + info.getName());
                communicationStarted = true;
            } else disconnectCurrentNode("Colon must separate the node name and address");
            else disconnectCurrentNode("Request must have three parts");
        } else disconnectCurrentNode(message);
    }

    private void handleNoneRequest() {
        String response = Node.sendEchoMessage(in, out);
        if (!response.equals("OHCE")) Node.sendEndRequest(out, "Node inactive, unexpected response");
    }

    private void handlePut(String[] parts) {
        // Assemble the key
        StringBuilder key = new StringBuilder();
        int keyLines = Integer.parseInt(parts[1]);
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');

        // Check if this node one of the nearest nodes to the generated hash ID
        String keyHashID = HashID.generate(key.toString());
        if (getNearestNodes(keyHashID).contains(info)) {
            // Assemble the value and store
            StringBuilder value = new StringBuilder();
            int valueLines = Integer.parseInt(parts[2]);
            for (int v = 0; v < valueLines; v++) value.append(Node.readNextLine(in)).append('\n');
            keyValues.put(key.toString(), value.toString());
            Node.send(out, "SUCCESS"); // Node is one of the nearest
        } else Node.send(out, "FAILED"); // Node is not one of the nearest
    }

    private void handleGet(String[] parts) {
        // Assemble and get the value
        int keyLines = Integer.parseInt(parts[1]);
        StringBuilder key = new StringBuilder();
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');
        String value = keyValues.get(key.toString());

        // Respond
        if (value != null) Node.send(out, "VALUE " + value.split("\n").length + '\n' + value);
        else Node.send(out, "NOPE");
    }

    private void handleEcho() {
        Node.send(out, "OHCE");
    }

    private void handleNotify() {
        String nodeName = Node.readNextLine(in);
        String nodeAddress = Node.readNextLine(in);
        addToNetworkMap(nodeName, nodeAddress);
        Node.send(out, "NOTIFIED");
    }

    private void handleNearest(String[] parts) {
        List<FullNodeInfo> nodeInfos = getNearestNodes(parts[1]);
        StringBuilder nodeInfoLines = new StringBuilder();
        for (FullNodeInfo nodeInfo : nodeInfos) {
            nodeInfoLines.append(nodeInfo.getName()).append('\n');
            nodeInfoLines.append(nodeInfo.getAddress()).append('\n');
        }
        Node.send(out, "NODES " + nodeInfos.size() + '\n' + nodeInfoLines);
    }

    private void close() {
        try {
            in.close();
            out.close();
            serverSocket.close();
            communicationStarted = false; // Break
            System.out.println("Communication ended.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnectCurrentNode(String reason) {
        Node.sendEndRequest(out, "Unexpected request: " + reason);
        removeFromNetworkMap(connectedNodeAddress);
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        communicationStarted = false;
    }

    private List<FullNodeInfo> getNearestNodes(String targetHashID) {
        List<NodeDistancePair> distancePairs = new ArrayList<>();

        //Iterate through all nodes in the network map
        for (Map.Entry<Integer, List<FullNodeInfo>> entry : networkMap.entrySet()) {
            for (FullNodeInfo node : entry.getValue()) {
                int distance = HashID.calculateDistance(targetHashID, HashID.generate(node.getAddress()));
                distancePairs.add(new NodeDistancePair(node, distance));
            }
        }

        // Sort nodes by distance
        Collections.sort(distancePairs);

        // Extract the top three closest nodes
        List<FullNodeInfo> closestNodes = new ArrayList<>();
        for (int i = 0; i < Math.min(3, distancePairs.size()); i++) {
            closestNodes.add(distancePairs.get(i).getNodeInfo());
        }

        return closestNodes;
    }
    private static class NodeDistancePair implements Comparable<NodeDistancePair> {
        private final FullNodeInfo nodeInfo;
        private final int distance;

        public NodeDistancePair(FullNodeInfo nodeInfo, int distance) {
            this.nodeInfo = nodeInfo;
            this.distance = distance;
        }

        public FullNodeInfo getNodeInfo() {
            return nodeInfo;
        }

        @Override
        public int compareTo(NodeDistancePair other) {
            return Integer.compare(distance, other.distance);
        }
    }
}
