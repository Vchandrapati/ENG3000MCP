
import org.example.Database;
import org.example.Processor;
import org.example.client.AbstractClient;
import org.example.client.BladeRunnerClient;
import org.example.client.MessageGenerator;
import org.example.client.ReasonEnum;
import org.example.messages.MessageSender;
import org.example.messages.Server;
import org.example.messages.StatHandler;
import org.example.state.MappingState;
import org.example.state.RunningState;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;
import org.example.state.WaitingState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class systemManagerTest {

    private static Database db;

    private static SystemStateManager sm;

    private static Server server;

    private static Field instanceFieldSM;
    private static Field instanceFieldDB;

    private static List<String> ids;
    private static List<ReasonEnum> reasons;

    @BeforeAll
    static void setupList() {
        server = Server.getInstance();
        db = Database.getInstanceTest();
        sm = SystemStateManager.getInstance();
        ids = new ArrayList<>();
        reasons = new ArrayList<>();

        try {
            instanceFieldSM = SystemStateManager.class.getDeclaredField("instance");
            instanceFieldSM.setAccessible(true);

            instanceFieldDB = Database.class.getDeclaredField("instance");
            instanceFieldDB.setAccessible(true);
        } catch (Exception e) {

        }

        ids.add(null);
        reasons.add(null);
        ids.add("");
        reasons.add(null);
        ids.add("what?");
        reasons.add(null);
        ids.add(null);
        reasons.add(ReasonEnum.COLLISION);
        ids.add("waht2?");
        reasons.add(ReasonEnum.COLLISION);
        ids.add("BR01");
        reasons.add(ReasonEnum.WRONGMESSAGE);
        ids.add("BR01");
        reasons.add(ReasonEnum.INCORTRIP);
        // 7
    }

    @BeforeEach
    void setUp() throws Exception {
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();
        for (int i = 1; i < 6; i++) {
            String id = "BR0" + i;
            db.addClient(id, new BladeRunnerClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 3000 + i, id)));
        }

        for (int i = 1; i < 11; i++) {
            String id = (i == 10) ? "CP" + i : "CP0" + i;
            db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 4000 + i, id), i));
        }

        // Set the field to null, effectively resetting the singleton
        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
    }

    @Test
    void sysManagerAddUnresponsiveInWaiting() throws Exception {
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);
    }

    @Test
    void sysManagerAddUnresponsiveInMapping() throws Exception {
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
        assertSame(sm.getState(), SystemState.EMERGENCY);


        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);
        sm.injectDatabase(db);

        assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
        assertSame(sm.getState(), SystemState.EMERGENCY);
    }

    @Test
    void sysManagerAddUnresponsiveInRunning() throws Exception {
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
        assertNotSame(sm.getState(), SystemState.EMERGENCY);


        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
        assertSame(SystemState.EMERGENCY, sm.getState());

        assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
        assertSame(sm.getState(), SystemState.EMERGENCY);


        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);
        sm.injectDatabase(db);

        assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
        assertSame(sm.getState(), SystemState.EMERGENCY);


        assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

    }

    @Test
    void sysManagerAddUnresponsiveInEmergency() throws Exception {
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
        assertSame(sm.getState(), SystemState.EMERGENCY);

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);
        sm.injectDatabase(db);

        assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
        assertSame(sm.getState(), SystemState.EMERGENCY);
    }



    @Test
    void sysManagerNeedstrip() throws Exception {
        sm.injectDatabase(db);

        assertTrue(sm.needsTrip(1, false));
        assertTrue(sm.needsTrip(1, true));

        assertTrue(sm.needsTrip(2, false));
        assertTrue(sm.needsTrip(2, true));

        assertTrue(sm.needsTrip(3, false));
        assertTrue(sm.needsTrip(3, true));

        assertTrue(sm.needsTrip(4, false));
        assertTrue(sm.needsTrip(4, true));

        assertTrue(sm.needsTrip(5, false));
        assertTrue(sm.needsTrip(5, true));

        assertTrue(sm.needsTrip(6, false));
        assertTrue(sm.needsTrip(6, true));

        assertTrue(sm.needsTrip(7, false));
        assertTrue(sm.needsTrip(7, true));

        assertTrue(sm.needsTrip(8, false));
        assertTrue(sm.needsTrip(8, true));

        assertTrue(sm.needsTrip(9, false));
        assertTrue(sm.needsTrip(9, true));

        assertTrue(sm.needsTrip(10, false));
        assertTrue(sm.needsTrip(10, true));

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.MAPPING);

        assertTrue(sm.needsTrip(1, false));
        assertTrue(sm.needsTrip(1, true));

        assertTrue(sm.needsTrip(2, false));
        assertTrue(sm.needsTrip(2, true));

        assertTrue(sm.needsTrip(3, false));
        assertTrue(sm.needsTrip(3, true));

        assertTrue(sm.needsTrip(4, false));
        assertTrue(sm.needsTrip(4, true));

        assertTrue(sm.needsTrip(5, false));
        assertTrue(sm.needsTrip(5, true));

        assertTrue(sm.needsTrip(6, false));
        assertTrue(sm.needsTrip(6, true));

        assertTrue(sm.needsTrip(7, false));
        assertTrue(sm.needsTrip(7, true));

        assertTrue(sm.needsTrip(8, false));
        assertTrue(sm.needsTrip(8, true));

        assertTrue(sm.needsTrip(9, false));
        assertTrue(sm.needsTrip(9, true));

        assertTrue(sm.needsTrip(10, false));
        assertTrue(sm.needsTrip(10, true));

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.RUNNING);

        assertFalse(sm.needsTrip(1, false));
        assertFalse(sm.needsTrip(1, true));

        assertFalse(sm.needsTrip(2, false));
        assertFalse(sm.needsTrip(2, true));

        assertFalse(sm.needsTrip(3, false));
        assertFalse(sm.needsTrip(3, true));

        assertFalse(sm.needsTrip(4, false));
        assertFalse(sm.needsTrip(4, true));

        assertFalse(sm.needsTrip(5, false));
        assertFalse(sm.needsTrip(5, true));

        assertFalse(sm.needsTrip(6, false));
        assertFalse(sm.needsTrip(6, true));

        assertFalse(sm.needsTrip(7, false));
        assertFalse(sm.needsTrip(7, true));

        assertFalse(sm.needsTrip(8, false));
        assertFalse(sm.needsTrip(8, true));

        assertFalse(sm.needsTrip(9, false));
        assertFalse(sm.needsTrip(9, true));

        assertFalse(sm.needsTrip(10, false));
        assertFalse(sm.needsTrip(10, true));

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance();
        sm.setState(SystemState.EMERGENCY);

        assertFalse(sm.needsTrip(1, false));
        assertFalse(sm.needsTrip(1, true));

        assertFalse(sm.needsTrip(2, false));
        assertFalse(sm.needsTrip(2, true));

        assertFalse(sm.needsTrip(3, false));
        assertFalse(sm.needsTrip(3, true));

        assertFalse(sm.needsTrip(4, false));
        assertFalse(sm.needsTrip(4, true));

        assertFalse(sm.needsTrip(5, false));
        assertFalse(sm.needsTrip(5, true));

        assertFalse(sm.needsTrip(6, false));
        assertFalse(sm.needsTrip(6, true));

        assertFalse(sm.needsTrip(7, false));
        assertFalse(sm.needsTrip(7, true));

        assertFalse(sm.needsTrip(8, false));
        assertFalse(sm.needsTrip(8, true));

        assertFalse(sm.needsTrip(9, false));
        assertFalse(sm.needsTrip(9, true));

        assertFalse(sm.needsTrip(10, false));
        assertFalse(sm.needsTrip(10, true));
    }

    @Test
    void sysManagersetState() throws Exception {
        sm = SystemStateManager.getInstance();

        assertFalse(sm.setState(null));
        assertFalse(sm.setState(SystemState.WAITING));
        assertTrue(sm.setState(SystemState.EMERGENCY));
        assertTrue(sm.setState(SystemState.MAPPING));
        assertTrue(sm.setState(SystemState.RUNNING));
    }

    @Test
    void sysManagerRun() throws Exception {
        sm.injectDatabase(db);
        WaitingState.injectDatabase(db);
        assertFalse(sm.run());
        waitJimmy(System.currentTimeMillis(), 6000);
        assertTrue(sm.run());

        instanceFieldSM.set(null, null);
        sm.injectDatabase(db);
        sm.setState(SystemState.RUNNING);
        org.example.state.RunningState.injectDatabase(db);
        assertFalse(sm.run());
        waitJimmy(System.currentTimeMillis(), 6000);
        assertFalse(sm.run());

        instanceFieldSM.set(null, null);
        sm.injectDatabase(db);
        sm.setState(SystemState.EMERGENCY);
        org.example.state.RunningState.injectDatabase(db);
        assertFalse(sm.run());
        waitJimmy(System.currentTimeMillis(), 6000);
        assertTrue(sm.run());

        instanceFieldSM.set(null, null);
        sm.injectDatabase(db);
        sm.setState(SystemState.MAPPING);
        org.example.state.RunningState.injectDatabase(db);
        assertFalse(sm.run());
        waitJimmy(System.currentTimeMillis(), 6000);
        assertTrue(sm.run());
    }

    @Test
    void waitingOperation() throws Exception {
        WaitingState ws = new WaitingState();
        ws.injectDatabase(db);
        assertTrue(ws.performOperation());

        ws = new WaitingState();
        instanceFieldDB.set(null, null);
        db = db.getInstance();
        ws.injectDatabase(db);

        assertFalse(ws.performOperation());
        waitJimmy(System.currentTimeMillis(), 2000);
        assertFalse(ws.performOperation());

        ws = new WaitingState();
        instanceFieldDB.set(null, null);
        db = db.getInstance();
        ws.injectDatabase(db);

        assertFalse(ws.performOperation());
        waitJimmy(System.currentTimeMillis(), 4500);
        assertTrue(ws.performOperation());
    }

    @Test
    void waitingOperation2() throws Exception {
        WaitingState ws = new WaitingState();
        db.fullPurge("BR01");

        ws.injectDatabase(db);
        assertFalse(ws.performOperation());
    }

    @Test
    void waitingOperation3() throws Exception {
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();

        for (int i = 1; i < 6; i++) {
            String id = "BR0" + i;
            db.addClient(id, new BladeRunnerClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 3000 + i, id)));
        }

        for (int i = 1; i < 10; i++) {
            String id = (i == 10) ? "CP" + i : "CP0" + i;
            db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 4000 + i, id), i));
        }

        WaitingState ws = new WaitingState();
        ws.injectDatabase(db);
        assertFalse(ws.performOperation());

    }

    @Test
    void runningOperation() throws Exception {
        org.example.state.RunningState rs = new org.example.state.RunningState();
        rs.injectDatabase(db);

        assertFalse(rs.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(rs.performOperation());

        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());

        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());

        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());

        assertFalse(rs.performOperation());
        assertFalse(rs.performOperation());
        assertFalse(rs.performOperation());
        assertFalse(rs.performOperation());

        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());

        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());
    }

    @Test
    void runningOperation2() throws Exception {
        org.example.state.RunningState rs = new org.example.state.RunningState();
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();
        rs.injectDatabase(db);

        assertFalse(rs.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);

        assertFalse(rs.performOperation());
        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());

        assertSame(rs.getNextState(), SystemState.EMERGENCY);
    }


    @Test
    void mapping() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState();
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();

        for (int i = 1; i < 6; i++) {
            String id = "BR0" + i;
            BladeRunnerClient c = new BladeRunnerClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 3000 + i, id));


            db.addClient(id, c);
        }


        for (int i = 1; i < 11; i++) {
            String id = (i == 10) ? "CP" + i : "CP0" + i;
            db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 4000 + i, id), i));
        }


        ms.injectDatabase(db);
        Processor.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(ms.performOperation());

        assertFalse(ms.performOperation());
        ms.addTrip(1, false);
        ms.addTrip(1, true);
        assertFalse(ms.performOperation());

        ms.addTrip(2, false);
        ms.addTrip(2, true);
        assertFalse(ms.performOperation());

        ms.addTrip(3, false);
        ms.addTrip(3, true);
        assertFalse(ms.performOperation());

        ms.addTrip(4, false);
        ms.addTrip(4, true);
        assertFalse(ms.performOperation());

        ms.addTrip(5, false);
        ms.addTrip(5, true);
        assertTrue(ms.performOperation());

    }

    @Test
    void mapping1() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState();
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();

        for (int i = 1; i < 6; i++) {
            String id = "BR0" + i;
            BladeRunnerClient c = new BladeRunnerClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 3000 + i, id));

            c.collision(true, new Object());

            db.addClient(id, c);
        }


        for (int i = 1; i < 11; i++) {
            String id = (i == 10) ? "CP" + i : "CP0" + i;
            db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 4000 + i, id), i));
        }


        ms.injectDatabase(db);
        Processor.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(ms.performOperation());

        assertFalse(ms.performOperation());
        ms.addTrip(1, false);
        ms.addTrip(1, true);
        assertFalse(ms.performOperation());

        ms.addTrip(2, false);
        ms.addTrip(2, true);
        assertFalse(ms.performOperation());

        ms.addTrip(3, false);
        ms.addTrip(3, true);
        assertFalse(ms.performOperation());

        ms.addTrip(4, false);
        ms.addTrip(4, true);
        assertFalse(ms.performOperation());

        ms.addTrip(5, false);
        ms.addTrip(5, true);
        assertTrue(ms.performOperation());
    }

    @Test
    void mapping2() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState();
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();
        ms.injectDatabase(db);
        Processor.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);

        assertTrue(ms.performOperation());
    }

    @Test
    void mapping3() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState();
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();

        for (int i = 1; i < 6; i++) {
            String id = "BR0" + i;
            BladeRunnerClient c = new BladeRunnerClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 3000 + i, id));


            db.addClient(id, c);
        }


        for (int i = 1; i < 11; i++) {
            String id = (i == 10) ? "CP" + i : "CP0" + i;
            db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
                    new MessageSender(server, InetAddress.getLocalHost(), 4000 + i, id), i));
        }


        ms.injectDatabase(db);
        Processor.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(ms.performOperation());

        assertFalse(ms.performOperation());
        ms.addTrip(10, false);
        ms.addTrip(10, true);
        assertFalse(ms.performOperation());

        ms.addTrip(6, false);
        ms.addTrip(6, true);
        assertFalse(ms.performOperation());

        ms.addTrip(3, false);
        ms.addTrip(3, true);
        assertFalse(ms.performOperation());

        ms.addTrip(4, false);
        ms.addTrip(4, true);
        assertFalse(ms.performOperation());
        assertFalse(ms.performOperation());

        waitJimmy(System.currentTimeMillis(), 16000);

        assertTrue(ms.performOperation());

        waitJimmy(System.currentTimeMillis(), 16000);

        assertTrue(ms.performOperation());

        waitJimmy(System.currentTimeMillis(), 16000);

        assertTrue(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 16000);

        assertTrue(ms.performOperation());

    }

    @Test
    void mapping4() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState();
        instanceFieldDB.set(null, null);
        db = Database.getInstanceTest();
        ms.injectDatabase(db);
        Processor.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);

        ms.addTrip(-10, false);
        ms.addTrip(-10, true);

        assertTrue(ms.performOperation());
    }


    // MAPPING STATE, may need to run process twice before going to the next br

    private void waitJimmy(long start, long sleep) {
        while (System.currentTimeMillis() - start <= sleep);
    }
}

