import org.example.Client;
import org.example.Server;
import org.example.TrainClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.example.Constants.PORT;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    private Server server;
    private DatagramSocket clientSocket;
    private DatagramSocket clientSocket2;

    @BeforeEach
    public void setUp() throws Exception {
        server = new Server();
        clientSocket = new DatagramSocket(2000, InetAddress.getByName("localhost"));

        String message = "Test message";
        byte[] buffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), PORT);
        clientSocket.send(sendPacket);

        // Second client for testing
        clientSocket2 = new DatagramSocket(3000, InetAddress.getByName("localhost"));
        clientSocket2.send(sendPacket);
    }

    @AfterEach
    public void tearDown() {
        server.stop();
        clientSocket.close();
    }

    @Test // Test message fields are serialised correctly
    void testMessageGenerator() throws Exception {

    }

    @Test // Test message format is correct
    void testRecieveFormat() throws Exception {

    }

    @Test // Test invalid messages dont crash system
    void testInvalidMessage() throws Exception {

    }

    @Test // Test multiple trains recieving messages at once doesnt block system
    void testSimultaneousMessage() throws Exception {

    }

    @Test // Test that the message is parsed correctly and states are changed
    void testMessageParsing() throws Exception {

    }

    @Test // Test that the message is parsed into database correctly
    void testDatabaseUpdate() throws Exception {

    }

    @Test // Test that the message from checkpoint is correctly read and updated
    void testCheckpointMessage() throws Exception {

    }
}
