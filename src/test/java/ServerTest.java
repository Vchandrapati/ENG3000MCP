// import org.example.Client;
// import org.example.Server;
// import org.example.TrainClient;
// import org.junit.jupiter.api.*;

// import java.net.DatagramPacket;
// import java.net.DatagramSocket;
// import java.net.InetAddress;

// import static org.example.Constants.PORT;
// import static org.junit.jupiter.api.Assertions.*;

// public class ServerTest {
//     private Server server;
//     private DatagramSocket clientSocket;

//     @BeforeEach
//     public void setUp() throws Exception {
//         server = new Server();
//         clientSocket = new DatagramSocket(2000, InetAddress.getByName("localhost"));
//     }

//     @AfterEach
//     public void tearDown() {
//         server.stop();
//         clientSocket.close();
//     }

//     @Test
//     void testConnectionListenerWithNewClient() throws Exception {
//         InetAddress clientAddress = InetAddress.getByName("localhost");
//         int clientPort = 2000;

//         // Prepare a DatagramPacket to simulate a client's message
//         String message = "Test message";
//         byte[] buffer = message.getBytes();
//         DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), PORT);
//         clientSocket.send(sendPacket); // Simulate sending the packet to the server

//         // Wait briefly to ensure the server processes the packet
//         Thread.sleep(500);

//         // Assert
//         assertEquals(1, server.clients.size());
//         System.out.println("here");
//         Client client = server.clients.getFirst();
//         assertNotNull(client);
//         assertEquals(clientAddress, client.clientAddress);
//         assertEquals(clientPort, client.clientPort);
//     }

//     @Test
//     void testConnectionListenerWithExistingClient() throws Exception {
//         InetAddress clientAddress = InetAddress.getByName("localhost");
//         int clientPort = 2000;

//         Client client = new TrainClient(clientAddress, clientPort, "BR 69");
//         server.clients.add(client);

//         // Prepare a DatagramPacket to simulate the client's message
//         String message = "Test message";
//         byte[] buffer = message.getBytes();
//         DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

//         Thread.sleep(100);

//         // Assert
//         assertEquals(clientAddress, client.clientAddress);
//         assertEquals(clientPort, client.clientPort);
//         assertNotNull(client);
//         assertTrue(server.clients.contains(client));
//     }

//     @Test
//     void testStatusScheduler() throws Exception {
//         Client client = new TrainClient(InetAddress.getByName("localhost"), 2000, "BR 69");
//         server.clients.add(client);

//         // Wait enough time to allow at least one scheduled task to run
//         byte[] buffer = new byte[1024];
//         DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

//         // Assert
//         assertNotNull(packet.getData());
//         assertTrue(packet.getLength() > 0);
//         System.out.println(packet);

//         // Cleanup
//         server.stop();
//         clientSocket.close();
//     }
// }
