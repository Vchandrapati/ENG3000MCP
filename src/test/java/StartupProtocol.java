import org.junit.jupiter.api.*;
import org.junit.platform.commons.logging.Logger;
import org.example.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import java.util.logging.Level;

import static org.example.Constants.PORT;
import static org.junit.jupiter.api.Assertions.*;
import static org.example.LoggerConfig.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestWatcher;

public class StartupProtocol {

    @BeforeEach
    public void setUp() throws Exception {
        Database db = Database.getInstance();
        for (int i = 1; i <= 5; i++) {
            db.addTrain("BR0"+i, new TrainClient(InetAddress.getByName("localhost"),1, "BR0"+i));
        }
        for (int i = 1; i <= 10; i++) {
            db.addStation("ST0"+i, new StationClient(InetAddress.getByName("localhost"),1, "ST0"+i));
            db.addCheckpoint("CH0"+i, new CheckpointClient(InetAddress.getByName("localhost"),1, "CH0"+i));
        }
        
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    void test1() throws Exception {
        App.main(null);
        long time = System.currentTimeMillis();
        while(App.isRunning) {
            if(System.currentTimeMillis() - time >= 10000) {

            }
        }
        //need tripped sensor to work properly to test
        //TODO
        
        //assertEquals(1, 1);
        //assertEquals("BR01", db.getTrain("BR01"));
    }

}
