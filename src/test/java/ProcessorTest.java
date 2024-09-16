import org.example.Server;
import org.example.SystemState;
import org.example.SystemStateManager;
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
import java.util.Optional;

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

        SystemStateManager.getInstance().setState(SystemState.RUNNING);

        t1.updateStatus("STARTED");
        t2.updateStatus("STARTED");
        t3.updateStatus("STARTED");
        t4.updateStatus("STARTED");

        db.addTrain("BR01", t1);
        db.addTrain("BR02", t2);
        //db.addTrain("BR03", t3);
        //db.addTrain("BR04", t3);

        db.updateTrainBlock("BR01", 4);
        db.updateTrainBlock("BR02", 6);

        t1.changeZone(4);
        t2.changeZone(6);
        //db.updateTrainBlock("BR03", 7);
        //db.updateTrainBlock("BR04", 8);

        server = Server.getInstance();

      
    }

    @AfterEach
    public void tearDown() {
    }

    @Test // Test trains distance are maintained 1 block
    public void testTrainDistance() throws Exception{
        p.sensorTripped(5);

        System.out.println(db.getLastTrainInBlock(5));

        assertEquals(t1.getStatus(), "STOPPED");
        assertEquals(t2.getStatus(), "STARTED");
        assertEquals(t3.getStatus(), "STARTED");
        assertEquals(t4.getStatus(), "STARTED");
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

    @Test // Test processor correctly reads database updates simultaneously
    void TrainZoneUpdate() throws Exception {

        assertEquals(Optional.ofNullable(t1.getZone()), Optional.of(4));
        assertEquals(Optional.ofNullable(t2.getZone()), Optional.of(6));


        p.sensorTripped(5);
        //t1 should stop now as t2 is in zone 6
        assertEquals(Optional.ofNullable(t1.getZone()), Optional.of(5));
        assertEquals(t1.getStatus(), "STOPPED");

        p.sensorTripped(7);
        p.sensorTripped(8);
        assertEquals(Optional.ofNullable(t2.getZone()), Optional.of(8));
        assertEquals(Optional.ofNullable(t1.getZone()),Optional.of(7));
        assertEquals(t1.getStatus(), "STOPPED");
        assertEquals(t2.getStatus(), "STARTED");

        // t2 should move to 7, causing t1 to move to 6
        //4,6,7,8
    }
}
