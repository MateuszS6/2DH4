public class FullNodeTest {
    public static void main(String[] args) {
        FullNode server = new FullNode();
        server.listen("127.0.0.1", 2345);
//        server.handleIncomingConnections("mateusz.stepien@city.ac.uk:MyNode", "127.0.0.1:2345");
    }
}
