import org.example.Client;
import org.example.Server;
import org.example.TrainClient;
import org.example.Database;
import org.example.App;
import org.junit.jupiter.api.*;
import org.example.CheckpointClient;
import org.example.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.*;

import static org.example.Constants.PORT;
import static org.junit.jupiter.api.Assertions.*;

public class StartupProtocol {
    private Server server;
    private List<DatagramSocket> clients = new ArrayList<>();

    private Database db = Database.getInstance();



    @BeforeEach
    public void setUp() throws Exception {
        for (int i = 1; i <= 5; i++) {
//            DatagramSocket br = new DatagramSocket(3000 + i, InetAddress.getByName("localhost"));
//            DatagramSocket st = new DatagramSocket(4000 + i, InetAddress.getByName("localhost"));
//            clients.add(br);
//            clients.add(st);
            db.addTrain("BR0"+i, new TrainClient(InetAddress.getByName("localhost"),1, "BR0"+i));
            db.addStation("st"+i, new StationClient(InetAddress.getByName("localhost"),1, "st"+i));
        }
        for (int i = 1; i <= 10; i++) {
            db.addCheckpoint("ch"+i, new CheckpointClient(InetAddress.getByName("localhost"),1, "ch"+i));
//            DatagramSocket ch = new DatagramSocket(5000 + i, InetAddress.getByName("localhost"));
//            clients.add(ch);
        }
        App.main(null);
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    void test1() throws Exception {

        assertEquals(1, 1);
        //assertEquals("br1", db.getTrain("br1"));
        System.out.println("gooer");

    }

}
