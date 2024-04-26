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
    private String connectedNodeName;
    private String connectedNodeAddress;
    private boolean started = false;
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
        info = new FullNodeInfo(startingNodeName, startingNodeAddress);
        connectedNodeName = startingNodeName;
        connectedNodeAddress = startingNodeAddress;
        for (int d = 0; d < 257; d++) networkMap.put(d, new ArrayList<>(3));
        addToNetworkMap(info);
//        addToNetworkMap("mateusz.stepien@city.ac.uk:test", "127.0.0.1:2345");
//        addToNetworkMap("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-1", "10.0.0.4:2244");
//        addToNetworkMap("martin.brain@city.ac.uk:MyCoolImplementation,1.67,test-node-7", "10.0.0.23:2400");
//        addToNetworkMap("martin.brain@city.ac.uk:MyCoolImplementation,1.67,test-node-9", "10.0.0.96:35035");

        // TODO: Search for full nodes and add to network when acting as client
//        notifyInfo();
//        List<FullNodeInfo> nodes = Node.sendNearestRequest(in, out, HashID.generate(info.getName()));
//        for (FullNodeInfo node : nodes) {
//            Node.sendStartRequest(in, out, info.getName());
//        }

        try {
            clientSocket = serverSocket.accept();
            System.out.println(" --- Connected!");

            // Create the reader and writer
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            if (!started) handleStart();
            while (started) {
                // Read and split first line of request
                String request = Node.readNextLine(in);
                if (request == null) {
                    disconnectCurrentNode("Empty line received");
                    break;
                }
                String[] requestParts = request.split(" ");

                if (request.startsWith("PUT?") && requestParts.length == 3) handlePut(requestParts);
                else if (request.startsWith("GET?") && requestParts.length == 2) handleGet(requestParts);
                else if (request.startsWith("ECHO?") && requestParts.length == 1) handleEcho();
                else if (request.startsWith("NOTIFY?") && requestParts.length == 1) handleNotify();
                else if (request.startsWith("NEAREST?") && requestParts.length == 2) handleNearest(requestParts);
                else if (request.startsWith("END") && requestParts.length == 2) close();
                else disconnectCurrentNode("Unexpected request");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void addToNetworkMap(FullNodeInfo newNodeInfo) {
        int distance = HashID.calculateDistance(info.getAddress(), newNodeInfo.getAddress());
        int nodesAtDistance = networkMap.get(distance).size();
        if (nodesAtDistance > 2) System.err.println("Max capacity reached at distance, adjusting...");
        while (nodesAtDistance > 2) {
            networkMap.get(distance).remove(0);
            nodesAtDistance = networkMap.get(distance).size();
        }
        networkMap.get(distance).add(newNodeInfo);
        System.out.println(newNodeInfo.getName() + " added at distance: " + distance + " -> " + networkMap.get(distance));
    }

    public void addToNetworkMap(String nodeName, String nodeAddress) {
        addToNetworkMap(new FullNodeInfo(nodeName, nodeAddress));
    }

    public void removeFromNetworkMap(String nodeAddress) {
        boolean found = false;
        for (Map.Entry<Integer, List<FullNodeInfo>> entry : networkMap.entrySet()) {
            for (FullNodeInfo nodeInfo : entry.getValue())
                if (nodeInfo.getAddress().equals(nodeAddress)) {
                    entry.getValue().remove(nodeInfo);
                    found = true;
                    break;
                }
            if (found) break;
        }
        if (!found) System.err.println("Node to remove not found in network map");
    }

    public void handleStart() {
        String message = Node.readNextLine(in);
        if (message.startsWith("START")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) if (parts[2].contains(":")) {
                Node.send(out, "START " + 1 + ' ' + info.getName());
                started = true;
            } else disconnectCurrentNode("Colon must separate the node name and address");
            else disconnectCurrentNode("Request must have three parts");
        } else disconnectCurrentNode("Unexpected request");
    }

    public void handlePut(String[] parts) {
        // Assemble the key and generate a hash ID
        StringBuilder key = new StringBuilder();
        int keyLines = Integer.parseInt(parts[1]);
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');
        String keyHashID = HashID.generate(key.toString());
//        System.out.println(keyHashID);

        // Check if this node one of the nearest nodes to the hash ID
        if (getNearestNodes(keyHashID).contains(info)) {
            // Assemble the value and store
            StringBuilder value = new StringBuilder();
            int valueLines = Integer.parseInt(parts[2]);
            for (int v = 0; v < valueLines; v++) value.append(Node.readNextLine(in)).append('\n');
            keyValues.put(key.toString(), value.toString());
            Node.send(out, "SUCCESS"); // Node is one of the nearest
        } else Node.send(out, "FAILED"); // Node is not one of the nearest
    }

    public void handleGet(String[] parts) {
        int keyLines = Integer.parseInt(parts[1]);
        StringBuilder key = new StringBuilder();
        for (int k = 0; k < keyLines; k++) key.append(Node.readNextLine(in)).append('\n');
        String value = keyValues.get(key.toString());

        if (value != null) Node.send(out, "VALUE " + value.split("\n").length + '\n' + value);
        else Node.send(out, "NOPE");
    }

    public void handleEcho() {
        Node.send(out, "OHCE");
    }

    public void notifyInfo() {
        String response = Node.sendNotifyRequest(in, out, info.getName(), info.getAddress());
        if (!response.equals("NOTIFIED")) disconnectCurrentNode("Unexpected request");
    }

    public void handleNotify() {
        String nodeName = Node.readNextLine(in);
        String nodeAddress = Node.readNextLine(in);
        addToNetworkMap(new FullNodeInfo(nodeName, nodeAddress));
        Node.send(out, "NOTIFIED");
    }

    public void handleNearest(String[] parts) {
        if (parts.length == 2) {
            List<FullNodeInfo> nodeInfos = getNearestNodes(parts[1]);
            StringBuilder nodeInfoLines = new StringBuilder();
            for (FullNodeInfo nodeInfo : nodeInfos) {
                nodeInfoLines.append(nodeInfo.getName()).append('\n');
                nodeInfoLines.append(nodeInfo.getAddress()).append('\n');
            }
            Node.send(out, "NODES " + nodeInfos.size() + '\n' + nodeInfoLines);
        } else disconnectCurrentNode("Unexpected request");
    }

    public void close() {
        try {
            in.close();
            out.close();
            serverSocket.close();
            started = false; // Break
            System.out.println("Communication ended.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnectCurrentNode(String reason) {
        Node.sendEndRequest(out, reason);
        removeFromNetworkMap(connectedNodeAddress);
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        started = false;
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
