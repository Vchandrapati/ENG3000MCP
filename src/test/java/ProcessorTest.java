import org.example.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.example.Constants.PORT;

public class ProcessorTest {
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
}
