
import org.example.Database;
import org.example.Processor;
import org.example.client.AbstractClient;
import org.example.client.BladeRunnerClient;
import org.example.client.MessageGenerator;
import org.example.client.ReasonEnum;
import org.example.state.MappingState;
import org.example.events.*;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.client.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class systemManagerTest {
    private static Database db;

    private static SystemStateManager sm;

    private static EventBus eb;

    private static Field instanceFieldSM;

    private static List<String> ids;
    private static List<ReasonEnum> reasons;
    private static List<BladeRunnerClient> brs;
    private static List<org.example.client.CheckpointClient> cps;

    @BeforeAll
    static void setupList() throws Exception {
        eb = EventBus.getInstance();

        db = new Database();

        brs = new ArrayList<>();
        cps = new ArrayList<>();

        for (int i = 1; i < 6; i++) {
            String id = "BR0" + i;
            brs.add(new BladeRunnerClient(id, new MessageGenerator(),
                    new MessageSender(InetAddress.getLocalHost(), 3000 + i, id, eb), 0));
        }

        for (int i = 1; i < 11; i++) {
            String id = (i == 10) ? "CP" + i : "CP0" + i;
            cps.add(new CheckpointClient(id, new MessageGenerator(),
                    new MessageSender(InetAddress.getLocalHost(), 4000 + i, id, eb), i, 0));
        }

        sm = SystemStateManager.getInstance(eb);
        ids = new ArrayList<>();
        reasons = new ArrayList<>();

        try {
            instanceFieldSM = SystemStateManager.class.getDeclaredField("instance");
            instanceFieldSM.setAccessible(true);

        } catch (Exception e) {
            System.out.println("yucky");
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
        db = new Database();
        for (BladeRunnerClient bladeRunnerClient : brs) {
            db.addClient(bladeRunnerClient.getId(), bladeRunnerClient);
        }
        for (org.example.client.CheckpointClient cp : cps) {
            db.addClient(cp.getId(), cp);
        }

        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance(eb);

    }

    public void resetSM() throws Exception{
        instanceFieldSM.set(null, null);
        sm = SystemStateManager.getInstance(eb);
        sm.injectDatabase(db);
    }

    @Test
    void sysManagerAddUnresponsiveInWaiting() throws Exception {
        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(0), reasons.get(0)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);

        resetSM();


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(1), reasons.get(1)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(2), reasons.get(2)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(3), reasons.get(3)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(4), reasons.get(4)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(5), reasons.get(5)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(6), reasons.get(6)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);
    }



    @Test
    void sysManagerAddUnresponsiveInMapping() throws Exception {
        sm.setState(SystemState.MAPPING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(0), reasons.get(0)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);

        resetSM();
        sm.setState(SystemState.MAPPING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(1), reasons.get(1)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.MAPPING);


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(2), reasons.get(2)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.MAPPING);


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(3), reasons.get(3)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);

        resetSM();
        sm.setState(SystemState.MAPPING);


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(4), reasons.get(4)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.MAPPING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(5), reasons.get(5)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(5), reasons.get(5)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);



        resetSM();
        sm.setState(SystemState.MAPPING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(6), reasons.get(6)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(6), reasons.get(6)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);
    }

    @Test
    void sysManagerAddUnresponsiveInRunning() throws Exception {
        sm.setState(SystemState.RUNNING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(0), reasons.get(0)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);

        resetSM();
        sm.setState(SystemState.RUNNING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(1), reasons.get(1)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.RUNNING);


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(2), reasons.get(2)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.RUNNING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(3), reasons.get(3)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.RUNNING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(4), reasons.get(4)));
        sm.run();
        assertNotSame(sm.currentState, SystemState.EMERGENCY);



        resetSM();
        sm.setState(SystemState.RUNNING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(5), reasons.get(5)));
        sm.run();
        assertSame(SystemState.EMERGENCY, sm.currentState);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(5), reasons.get(5)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);



        resetSM();
        sm.setState(SystemState.RUNNING);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(6), reasons.get(6)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);


        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(6), reasons.get(6)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);

    }

    @Test
    void sysManagerAddUnresponsiveInEmergency() throws Exception {
        assertSame(sm.currentState, SystemState.WAITING);
        sm.setState(SystemState.EMERGENCY);
        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(0), reasons.get(0)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(1), reasons.get(1)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);

        resetSM();
        sm.setState(SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(2), reasons.get(2)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(3), reasons.get(3)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(4), reasons.get(4)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(5), reasons.get(5)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);


        resetSM();
        sm.setState(SystemState.EMERGENCY);

        sm.addUnresponsiveClient(new ClientErrorEvent(ids.get(6), reasons.get(6)));
        sm.run();
        assertSame(sm.currentState, SystemState.EMERGENCY);
    }


    @Test
    void sysManagersetState() throws Exception {
        sm.setState(SystemState.WAITING);
        assertSame(sm.currentState, SystemState.WAITING);
        sm.setState(SystemState.MAPPING);
        assertSame(sm.currentState, SystemState.MAPPING);
        sm.setState(SystemState.RUNNING);
        assertSame(sm.currentState, SystemState.RUNNING);
        sm.setState(SystemState.EMERGENCY);
        assertSame(sm.currentState, SystemState.EMERGENCY);
    }


    @Test
    void waitingOperation() throws Exception {
        WaitingState ws = new WaitingState();
        ws.injectDatabase(db);
        assertTrue(ws.performOperation());

        db = new Database();
        ws = new WaitingState();
        ws.injectDatabase(db);

        assertFalse(ws.performOperation());
        waitJimmy(System.currentTimeMillis(), 2000);
        assertFalse(ws.performOperation());
    }

    @Test
    void waitingOperation2() throws Exception {
        WaitingState ws = new WaitingState();
        ws.injectDatabase(db);
        db.fullPurge("BR01");

        assertFalse(ws.performOperation());
    }

    @Test
    void waitingOperation3() throws Exception {
        db = new Database();
        db.fullPurge("CP10");

        sm.run();
        assertTrue(sm.currentState == SystemState.WAITING);

    }

    @Test
    void runningOperation() throws Exception {
        org.example.state.RunningState rs = new org.example.state.RunningState();

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

        assertFalse(rs.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);

        assertFalse(rs.performOperation());
        waitJimmy(System.currentTimeMillis(), 2500);
        assertFalse(rs.performOperation());

        assertSame(rs.getNextState(), SystemState.EMERGENCY);
    }


    @Test
    void mapping() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState(eb);
        ms.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(ms.performOperation());

        assertFalse(ms.performOperation());
        ms.addTrip(1, false);
        ms.addTrip(1, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(2, false);
        ms.addTrip(2, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(3, false);
        ms.addTrip(3, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(4, false);
        ms.addTrip(4, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(5, false);
        ms.addTrip(5, true);
        ms.performOperation();
        assertTrue(ms.performOperation());

    }

    @Test
    void mapping1() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState(eb);
        ms.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(ms.performOperation());

        assertFalse(ms.performOperation());
        ms.addTrip(1, false);
        ms.addTrip(1, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(2, false);
        ms.addTrip(2, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(3, false);
        ms.addTrip(3, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(4, false);
        ms.addTrip(4, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(5, false);
        ms.addTrip(5, true);
        ms.performOperation();
        assertTrue(ms.performOperation());
    }


    @Test
    void mapping3() throws Exception {
        org.example.state.MappingState ms = new org.example.state.MappingState(eb);
        MappingState.injectDatabase(db);

        assertFalse(ms.performOperation());
        waitJimmy(System.currentTimeMillis(), 3500);
        assertFalse(ms.performOperation());

        assertFalse(ms.performOperation());
        ms.addTrip(10, false);
        ms.addTrip(10, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(6, false);
        ms.addTrip(6, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(3, false);
        ms.addTrip(3, true);
        ms.performOperation();
        assertFalse(ms.performOperation());

        ms.addTrip(4, false);
        ms.addTrip(4, true);
        assertFalse(ms.performOperation());
        assertFalse(ms.performOperation());

        //waitJimmy(System.currentTimeMillis(), 16000);

        ms.addTrip(5, false);
        ms.addTrip(5, true);
        ms.performOperation();
        assertTrue(ms.performOperation());

    }

    @Test
    void sm1() throws Exception {
        sm.handleTrip(new TripEvent(0, false));
        sm.setState(SystemState.RUNNING);
        sm.handleTrip(new TripEvent(0, false));
        sm.setState(SystemState.EMERGENCY);
        sm.handleTrip(new TripEvent(0, false));

        sm.setState(SystemState.MAPPING);
        sm.handleTrip(new TripEvent(0, false));

        sm.setState(null);
        sm.setState(SystemState.MAPPING);
        sm.shutdown();

        sm.updateState(new NewStateEvent(SystemState.EMERGENCY));
    }

    @Test
    void running100() throws Exception {
        RunningState.injectDatabase(db);
        sm.setState(SystemState.RUNNING);
    }


    @Test
    void waiting() throws Exception {
        sm.injectDatabase(db);
        WaitingState.injectDatabase(db);

        sm.run();
        assertNotSame(sm.currentState, SystemState.MAPPING);
        waitJimmy(System.currentTimeMillis(), 5000);
        sm.run();

        assertSame(sm.currentState, SystemState.MAPPING);
    }


    // MAPPING STATE, may need to run process twice before going to the next br

    private void waitJimmy(long start, long sleep) {
        while (System.currentTimeMillis() - start <= sleep);
    }
}
