import org.junit.jupiter.api.*;
import org.example.*;

class StateTest {
    Database db = Database.getInstance();

    @BeforeEach
    public void setUp() throws Exception {
        for (int i = 1; i <= 4; i++) {
            //db.addClient("BR0"+i, new BladeRunnerClient(InetAddress.getByName("localhost"),1, "BR0"+i));
        }
        for (int i = 1; i <= 10; i++) {
            //db.addClient("ST0"+i, new StationClient(InetAddress.getByName("localhost"),1, "ST0"+i));
            //db.addClient("CH0"+i, new CheckpointClient(InetAddress.getByName("localhost"),1, "CH0"+i));
        }

    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    void testStartupTimeoutStart() throws Exception {
        App.main(null);
        long time = System.currentTimeMillis();
        Processor p = new Processor();
        int count = 1;
        Thread.sleep(12000);
        while(App.isRunning()) {
            if(System.currentTimeMillis() - time >= 2000 && count < 6) {
                time = System.currentTimeMillis();
                p.checkpointTripped(count);
                count++;
            }
            if(count == 6) {
                Thread.sleep(10000);
                break;
            }
        }
        App.shutdown();
    }

    @Test
    void testEarlyStartup() throws Exception {
        App.main(null);
        long time = System.currentTimeMillis();
        Processor p = new Processor();
        int count = 1;
        Thread.sleep(5000);

        while(App.isRunning()) {
            if(System.currentTimeMillis() - time >= 2000 && count < 6) {
                time = System.currentTimeMillis();
                p.checkpointTripped(count);
                count++;
            }
            // if(count == 6) {
            //     Thread.sleep(10000);
            //     break;
            // }
        }
        App.shutdown();
    }

    @Test
    void RestartupTest() throws Exception {
        App.main(null);
        long time = System.currentTimeMillis();
        Processor p = new Processor();
        int count = 1;
        Thread.sleep(5000);

        while(App.isRunning()) {
            if(System.currentTimeMillis() - time >= 2000) {
                time = System.currentTimeMillis();
                p.checkpointTripped(count);
                count++;
            }
            if(count == 6) {
                 Thread.sleep(3000);
                 //db.TESTING("BR99", new BladeRunnerClient(InetAddress.getByName("localhost"),1, "BR99"));
                 count++;
            }
            if(count == 10) break;
        }
        App.shutdown();
    }

}