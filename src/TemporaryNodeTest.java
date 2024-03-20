public class TemporaryNodeTest {
    public static void main(String[] args) {
        TemporaryNode client = new TemporaryNode();
        if (client.start("mateusz.stepien@city.ac.uk:MyNode", "127.0.0.1:2345")) {
            System.out.println("START complete");
        }
    }
}
