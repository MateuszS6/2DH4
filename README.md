# 2D#4 Program

2D#4 (pronounced "two-dee-hash-four") is a peer-to-peer distributed hash table implementation of the 2D#4 protocol, designed as part of the IN2011 Computer Networks coursework. This project facilitates efficient data storage and retrieval across a decentralized network environment. It includes implementations of both `FullNode` and `TemporaryNode` types, where `FullNode` handles robust data management and network communication, while `TemporaryNode` serves to facilitate temporary data operations and querying within the network. The dynamic nature of this implementation allows for real-time updates and interactions across the distributed network.

## Coursework Details

- **Course**: IN2011 Computer Networks
- **Institution**: City, University of London
- **Term**: Spring 2024

## Getting Started

### Prerequisites
- Java JDK 11 or higher
- Network access configured to allow TCP/IP communication

### Building the Project
To compile the project, navigate to the source directory and execute the following command in your terminal:
```bash
javac *.java
```

This command compiles all Java source files in the directory.

### Running the Application

**Start a FullNode**

To run a FullNode in the network, use the following command:
```bash
java CmdLineFullNode <startingNodeName> <startingNodeAddress> <ipAddress> <portNumber>
```

Replace the placeholders with actual values. For example:
```bash
java CmdLineFullNode "InitialNode" "192.168.1.1:5000" "192.168.1.2" 5001
```

**Start a TemporaryNode**

To perform operations from a TemporaryNode:
```bash
java CmdLineGet <startingNodeName> <startingNodeAddress> <key>
```

For storing data:
```bash
java CmdLineStore <startingNodeName> <startingNodeAddress> <key> <value>
```

## Features

- Node Communication: Supports TCP/IP based communication between nodes.
- Data Storage and Retrieval: Nodes can store and retrieve key-value pairs.
- Network Mapping: Implements basic network mapping functionalities.

## Current Status

- Node Communication: Fully implemented and tested.
- Data Storage and Retrieval: Fully functional.
- Network Discovery and Mapping: Partially implemented.

## Known Issues

- Network mapping does not automatically update when nodes join or leave.
- No timeout mechanism for node responses, potentially leading to hangs.

## Future Enhancements

- Security: Implement SSL/TLS for secure communications.
- Robust Error Handling: Enhance error handling and logging capabilities.
- Dynamic Network Mapping: Automatically update network maps based on node status changes.
