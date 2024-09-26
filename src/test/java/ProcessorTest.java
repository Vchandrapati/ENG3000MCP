import org.example.Server;
import org.example.SystemState;
import org.example.SystemStateManager;
import org.example.BladeRunnerClient;
import org.example.Database;
import org.example.Processor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ProcessorTest {
    private Server server;
    private DatagramSocket clientSocket;
    private DatagramSocket clientSocket2;
    private Database db = Database.getInstance();
    private Processor p = new Processor();

 BladeRunnerClient t1 = new BladeRunnerClient(InetAddress.getLoopbackAddress(), 2000, "BR01");
 BladeRunnerClient t2 = new BladeRunnerClient(InetAddress.getLoopbackAddress(), 2000, "BR02");
 BladeRunnerClient t3 = new BladeRunnerClient(InetAddress.getLoopbackAddress(), 2000, "BR03");
 BladeRunnerClient t4 = new BladeRunnerClient(InetAddress.getLoopbackAddress(), 2000, "BR04");

    @BeforeEach
    public void setUp() throws Exception {

        SystemStateManager.getInstance().setState(SystemState.RUNNING);

        t1.updateStatus("STARTED");
        t2.updateStatus("STARTED");
        t3.updateStatus("STARTED");
        t4.updateStatus("STARTED");


     db.updateBladeRunnerBlock("BR01", 4);
     db.updateBladeRunnerBlock("BR02", 6);

     t1.changeZone(4);
     t2.changeZone(6);
     //db.updateBladeRunnerBlock("BR03", 7);
     //db.updateBladeRunnerBlock("BR04", 8);

    }

    @AfterEach
    public void tearDown() {
    }

    @Test // Test processor identifies collisions and stops them
    void testCollissionDetection() throws Exception {

    }

 @Test // Test processor identifies malfunctioning BladeRunners
 void testBrokenBladeRunner() throws Exception {

    }

    @Test // Test processor correctly reads database updates simultaneously
    void testDatabaseUpdate() throws Exception {

    }

 @Test // Test processor correctly reads database updates simultaneously
 void BladeRunnerZoneUpdate() throws Exception {

        assertEquals(Optional.ofNullable(t1.getZone()), Optional.of(4));
        assertEquals(Optional.ofNullable(t2.getZone()), Optional.of(6));

        p.checkpointTripped(5);
        // t1 should stop now as t2 is in zone 6
        assertEquals(Optional.ofNullable(t1.getZone()), Optional.of(5));
        assertEquals(t1.getStatus(), "STOPPED");

        p.checkpointTripped(7);
        p.checkpointTripped(8);
        assertEquals(Optional.ofNullable(t2.getZone()), Optional.of(8));
        assertEquals(Optional.ofNullable(t1.getZone()), Optional.of(7));
        assertEquals(t1.getStatus(), "STOPPED");
        assertEquals(t2.getStatus(), "STARTED");

        // t2 should move to 7, causing t1 to move to 6
        // 4,6,7,8
    }
}
