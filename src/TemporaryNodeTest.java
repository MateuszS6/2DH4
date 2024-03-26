public class TemporaryNodeTest {
    public static void main(String[] args) {
        TemporaryNode client = new TemporaryNode();
        boolean start = client.start("mateusz.stepien@city.ac.uk:MyNode", "127.0.0.1:2345");
        System.out.println(start ? " -> START worked <-" : " -x START failed x-");

        boolean store = client.store("Hello there", "General Kenobi");
        System.out.println(store ? " -> STORE worked <-" : " -x STORE failed x-");

    }
}
