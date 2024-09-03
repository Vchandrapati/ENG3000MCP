import org.example.Client;
import org.example.MessageHandler;
import org.example.RecieveMessage;
import org.example.SendMessage;
import org.example.Server;
import org.example.TrainClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.example.Constants.PORT;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    private Server server;
    private DatagramSocket clientSocket;
    private DatagramSocket clientSocket2;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws Exception {
        server = new Server();
        clientSocket = new DatagramSocket(2000, InetAddress.getByName("localhost"));
        clientSocket2 = new DatagramSocket(3000, InetAddress.getByName("localhost"));
    }

    @AfterEach
    public void tearDown() {
        server.stop();
        clientSocket.close();
    }

    @Test // Test message fields are serialised correctly
    void testCCPMessageGenerator() throws Exception {
        InetAddress clientAddress = InetAddress.getLoopbackAddress();
        int clientPort = 2000;

        TrainClient tc = new TrainClient(clientAddress, clientPort, "BR01");
        tc.sendExecuteMessage(2);

        Thread.sleep(500);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        clientSocket.receive(packet);

        String message = new String(packet.getData(), 0, packet.getLength());
        SendMessage recieveMessage = objectMapper.readValue(message, SendMessage.class);

        assertEquals(recieveMessage.clientType, "ccp");
        assertEquals(recieveMessage.clientID, "BR01");
        assertEquals(recieveMessage.message, "EXEC");
        assertEquals(recieveMessage.action, "FAST");
    }

    @Test // Test invalid messages dont crash system
    void testInvalidMessage() throws Exception {
        InetAddress servAddress = InetAddress.getByName("localhost");
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, servAddress, 6666);
        clientSocket.send(packet);

        InetAddress clientAddress = InetAddress.getByName("localhost");
        int clientPort = 2000;

        TrainClient tc = new TrainClient(clientAddress, clientPort, "BR01");
        tc.sendExecuteMessage(2);

        Thread.sleep(1000);

        buffer = new byte[1024];
        packet = new DatagramPacket(buffer, buffer.length);
        clientSocket.receive(packet);

        String message = new String(packet.getData(), 0, packet.getLength());
        SendMessage recieveMessage = objectMapper.readValue(message, SendMessage.class);

        assertEquals(recieveMessage.clientType, "ccp");
        assertEquals(recieveMessage.clientID, "BR01");
        assertEquals(recieveMessage.message, "EXEC");
        assertEquals(recieveMessage.action, "FAST");
    }

    @Test // Test multiple trains recieving messages at once doesnt block system
    void testSimultaneousMessage() throws Exception {
        InetAddress clientAddress = InetAddress.getByName("localhost");
        int clientPort = 2000;

        RecieveMessage message = new RecieveMessage();
        message.clientType = "ccp";
        message.message = "CCIN";
        message.clientID = "BR01";
        message.timestamp = System.currentTimeMillis();

        String m = convertToJson(message);

        byte[] buffer = m.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

        InetAddress clientAddress2 = InetAddress.getByName("localhost");
        int clientPort2 = 2001;

        RecieveMessage message2 = new RecieveMessage();
        message2.clientType = "ccp";
        message2.message = "CCIN";
        message2.clientID = "BR02";
        message2.timestamp = System.currentTimeMillis();

        String m2 = convertToJson(message2);

        byte[] buffer2 = m2.getBytes();
        DatagramPacket sendPacket2 = new DatagramPacket(buffer2, buffer2.length, clientAddress2, clientPort2);

        clientSocket.send(sendPacket);
        clientSocket.send(sendPacket2);

        // Assert statements

    }

    @Test // Test that the message is parsed correctly and states are changed
    void testMessageParsing() throws Exception {
        // Prepare the CCIN message
        RecieveMessage message = new RecieveMessage();
        message.clientType = "ccp";
        message.message = "CCIN";
        message.clientID = "BR69";
        message.timestamp = System.currentTimeMillis();

        String jsonMessage = convertToJson(message);
        byte[] buffer = jsonMessage.getBytes("UTF-8");
        System.out.println(jsonMessage);

        // Send the CCIN message to the server
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), PORT);
        clientSocket.send(sendPacket);

        Thread.sleep(1000);

        // Prepare to receive the AKIN response
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        clientSocket.receive(receivePacket);

        // Convert the received data to a SendMessage object
        String receivedJson = new String(receivePacket.getData(), 0, receivePacket.getLength());
        SendMessage receivedMessage = objectMapper.readValue(receivedJson, SendMessage.class);

        // Validate the response
        assertEquals("ccp", receivedMessage.clientType);
        assertEquals("BR69", receivedMessage.clientID);
        assertEquals("AKIN", receivedMessage.message);
    }

    @Test // Test that the message is parsed into database correctly
    void testDatabaseUpdate() throws Exception {

    }

    private static String convertToJson(RecieveMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
