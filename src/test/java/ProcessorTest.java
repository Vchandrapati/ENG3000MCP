import org.example.Server;
import org.example.TrainClient;
import org.example.Database;
import org.example.Processor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Trim;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.example.Constants.PORT;
import static org.junit.Assert.assertEquals;

public class ProcessorTest {
    private Server server;
    private DatagramSocket clientSocket;
    private DatagramSocket clientSocket2;
    private Database db = Database.getInstance();
    private Processor p = new Processor();

    TrainClient t1 = new TrainClient(InetAddress.getLoopbackAddress(), 2000, "BR01");
    TrainClient t2 = new TrainClient(InetAddress.getLoopbackAddress(), 2000, "BR02");
    TrainClient t3 = new TrainClient(InetAddress.getLoopbackAddress(), 2000, "BR03");
    TrainClient t4 = new TrainClient(InetAddress.getLoopbackAddress(), 2000, "BR04");

    @BeforeEach
    public void setUp() throws Exception {
        t1.updateStatus("STARTED");
        t2.updateStatus("STARTED");
        t3.updateStatus("STARTED");
        t4.updateStatus("STARTED");

        db.addTrain("BR01", t1);
        db.addTrain("BR02", t2);
        db.addTrain("BR03", t3);
        db.addTrain("BR04", t3);

        db.updateTrainBlock("BR01", 4);
        db.updateTrainBlock("BR02", 6);
        db.updateTrainBlock("BR03", 7);
        db.updateTrainBlock("BR04", 8);

        System.out.println("hi");
        server = new Server();

      
    }

    @AfterEach
    public void tearDown() {
        clientSocket.close();
    }

    @Test // Test trains distance are maintained 1 block
    public void testTrainDistance() throws Exception{
        assertEquals(1, 1);
        p.sensorTripped(5);

        
        System.out.println("hi");
        assertEquals(t1.getStatus(), "STOPPED");
        System.out.println("bye");
        
    }

    @Test // Test processor identifies collisions and stops them
    void testCollissionDetection() throws Exception {

    }

    @Test // Test processor identifies malfunctioning trains
    void testBrokenTrain() throws Exception {

    }

    @Test // Test processor correctly reads database updates simultaneously
    void testDatabaseUpdate() throws Exception {

    }
}
