import org.example.Database;
import org.example.messages.Server;
import org.junit.jupiter.api.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    private static Server server;

    @BeforeAll
    public static void setUp() {
        server = Server.getInstance();
    }

    @AfterAll
    public static void tearDown() {
        server.shutdown();
    }

    @Test
    public void testServerReceivesPacket() throws Exception {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        TestLogHandler handler = new TestLogHandler();
        logger.addHandler(handler);

        String testMessage = "Hello, Server!";
        byte[] sendData = testMessage.getBytes(StandardCharsets.UTF_8);
        InetAddress serverAddress = InetAddress.getByName("localhost");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, Server.PORT);

        // Act
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.send(sendPacket);
        }

        Thread.sleep(500); // Wait for processing

        assertTrue(handler.getLogMessages().stream().anyMatch(msg -> msg.contains("handleMessage")));
        logger.removeHandler(handler);
    }

    @Test
    public void testSystemStateManagerOnHighThroughput() throws Exception {
        String testMessage = "Stress Test";
        byte[] sendData = testMessage.getBytes(StandardCharsets.UTF_8);
        InetAddress serverAddress = InetAddress.getByName("localhost");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, Server.PORT);

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            for (int i = 0; i < 60; i++) {
                clientSocket.send(sendPacket);
            }
        }

        Thread.sleep(1000); // Wait for processing

        // Assert
        assertTrue(Database.getInstance().getAllUnresponsiveClientIDs().size() > 1);
    }

    // Custom log handler to capture log messages
    class TestLogHandler extends Handler {
        private List<String> logMessages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            logMessages.add(record.getMessage());
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

        public List<String> getLogMessages() {
            return logMessages;
        }
    }
}
