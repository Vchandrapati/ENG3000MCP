
// import org.example.Database;
// import org.example.Processor;
// import org.example.client.AbstractClient;
// import org.example.client.BladeRunnerClient;
// import org.example.client.MessageGenerator;
// import org.example.client.ReasonEnum;
// import org.example.messages.MessageSender;
// import org.example.messages.Server;
// import org.example.state.MappingState;
// import org.example.events.*;
// import org.example.state.RunningState;
// import org.example.state.SystemState;
// import org.example.state.SystemStateManager;
// import org.example.state.WaitingState;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import java.lang.reflect.Field;
// import java.lang.reflect.Method;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collections;
// import java.net.InetAddress;
// import java.util.List;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// class systemManagerTest {

//     private static Database db;

//     private static SystemStateManager sm;

//     private static EventBus eb;

//     private static Field instanceFieldSM;
//     private static Field instanceFieldSER;

//     private static List<String> ids;
//     private static List<ReasonEnum> reasons;

//     private static MessageSender server;

//     @BeforeAll
//     static void setupList() {
//         eb = EventBus.getInstance();
//         db = new Database();
//         sm = SystemStateManager.getInstance(eb);
//         ids = new ArrayList<>();
//         reasons = new ArrayList<>();
//         server = 

//         try {
//             instanceFieldSM = SystemStateManager.class.getDeclaredField("instance");
//             instanceFieldSM.setAccessible(true);

//             instanceFieldSER = Server.class.getDeclaredField("instance");
//             instanceFieldSER.setAccessible(true);
//         } catch (Exception e) {
//             System.out.println("yucky");
//         }

//         ids.add(null);
//         reasons.add(null);
//         ids.add("");
//         reasons.add(null);
//         ids.add("what?");
//         reasons.add(null);
//         ids.add(null);
//         reasons.add(ReasonEnum.COLLISION);
//         ids.add("waht2?");
//         reasons.add(ReasonEnum.COLLISION);
//         ids.add("BR01");
//         reasons.add(ReasonEnum.WRONGMESSAGE);
//         ids.add("BR01");
//         reasons.add(ReasonEnum.INCORTRIP);
//         // 7
//     }


//     @BeforeEach
//     void setUp() throws Exception {
//         db = new Database();

//         for (int i = 1; i < 6; i++) {
//             String id = "BR0" + i;
//             db.addClient(id, new BladeRunnerClient(id, new MessageGenerator(),
//                     new MessageSender(InetAddress.getLocalHost(), 3000 + i, id), 0));
//         }

//         for (int i = 1; i < 11; i++) {
//             String id = (i == 10) ? "CP" + i : "CP0" + i;
//             db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
//                     new MessageSender(InetAddress.getLocalHost(), 4000 + i, id), i, 0));
//         }

//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);

//         instanceFieldSER.set(null, null);
//     }

//     @Test
//     void sysManagerAddUnresponsiveInWaiting() throws Exception {
//         eb.publish(new ClientErrorEvent(ids.get(0), reasons.get(0)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);


//         eb.publish(new ClientErrorEvent(ids.get(1), reasons.get(1)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);

//         eb.publish(new ClientErrorEvent(ids.get(2), reasons.get(2)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);


//         eb.publish(new ClientErrorEvent(ids.get(3), reasons.get(3)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);

//         eb.publish(new ClientErrorEvent(ids.get(4), reasons.get(4)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);

//         eb.publish(new ClientErrorEvent(ids.get(5), reasons.get(5)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);

//         eb.publish(new ClientErrorEvent(ids.get(6), reasons.get(6)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);
//     }

//     @Test
//     void sysManagerAddUnresponsiveInMapping() throws Exception {
//         sm.setState(SystemState.MAPPING);

//         eb.publish(new ClientErrorEvent(ids.get(0), reasons.get(0)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.MAPPING);

//         eb.publish(new ClientErrorEvent(ids.get(1), reasons.get(1)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.MAPPING);


//         eb.publish(new ClientErrorEvent(ids.get(2), reasons.get(2)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.MAPPING);


//         eb.publish(new ClientErrorEvent(ids.get(3), reasons.get(3)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.MAPPING);


//         eb.publish(new ClientErrorEvent(ids.get(4), reasons.get(4)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.MAPPING);


//         eb.publish(new ClientErrorEvent(ids.get(5), reasons.get(5)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(5), reasons.get(5)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);


//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.MAPPING);

//         eb.publish(new ClientErrorEvent(ids.get(6), reasons.get(6)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(6), reasons.get(6)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);
//     }

//     @Test
//     void sysManagerAddUnresponsiveInRunning() throws Exception {
//         sm.setState(SystemState.RUNNING);

//         eb.publish(new ClientErrorEvent(ids.get(0), reasons.get(0)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.RUNNING);

//         eb.publish(new ClientErrorEvent(ids.get(1), reasons.get(1)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.RUNNING);


//         eb.publish(new ClientErrorEvent(ids.get(2), reasons.get(2)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.RUNNING);

//         eb.publish(new ClientErrorEvent(ids.get(3), reasons.get(3)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.RUNNING);

//         eb.publish(new ClientErrorEvent(ids.get(4), reasons.get(4)));
//         assertNotSame(sm.currentState, SystemState.EMERGENCY);


//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.RUNNING);

//         eb.publish(new ClientErrorEvent(ids.get(5), reasons.get(5)));
//         assertSame(SystemState.EMERGENCY, sm.currentState);

//         eb.publish(new ClientErrorEvent(ids.get(5), reasons.get(5)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);


//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);

//         eb.publish(new ClientErrorEvent(ids.get(6), reasons.get(6)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);


//         eb.publish(new ClientErrorEvent(ids.get(6), reasons.get(6)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//     }

//     @Test
//     void sysManagerAddUnresponsiveInEmergency() throws Exception {
//         eb.publish(new NewStateEvent(SystemState.EMERGENCY));

//         eb.publish(new ClientErrorEvent(ids.get(0), reasons.get(0)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(1), reasons.get(1)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(2), reasons.get(2)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(3), reasons.get(3)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(4), reasons.get(4)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(5), reasons.get(5)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);

//         db = new Database();
//         instanceFieldSM.set(null, null);
//         sm = SystemStateManager.getInstance(eb);
//         sm.setState(SystemState.EMERGENCY);

//         eb.publish(new ClientErrorEvent(ids.get(6), reasons.get(6)));
//         assertSame(sm.currentState, SystemState.EMERGENCY);
//     }


//     @Test
//     void sysManagersetState() throws Exception {
//         eb.publish(new NewStateEvent(SystemState.WAITING));
//         assertSame(sm.currentState, SystemState.WAITING);
//         eb.publish(new NewStateEvent(SystemState.MAPPING));
//         assertSame(sm.currentState, SystemState.MAPPING);
//         eb.publish(new NewStateEvent(SystemState.RUNNING));
//         assertSame(sm.currentState, SystemState.RUNNING);
//         eb.publish(new NewStateEvent(SystemState.EMERGENCY));
//         assertSame(sm.currentState, SystemState.EMERGENCY);
//     }

//     @Test
//     void sysManagerRun() throws Exception {
//         db = new Database();
//         assertSame(sm.currentState, SystemState.WAITING);

//         for (int i = 1; i < 6; i++) {
//             String id = "BR0" + i;
//             db.addClient(id, new BladeRunnerClient(id, new MessageGenerator(),
//                     new MessageSender(InetAddress.getLocalHost(), 3000 + i, id), 0));
//         }

//         for (int i = 1; i < 11; i++) {
//             String id = (i == 10) ? "CP" + i : "CP0" + i;
//             db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
//                     new MessageSender(InetAddress.getLocalHost(), 4000 + i, id), i, 0));
//         }

//         waitJimmy(System.currentTimeMillis(), 6000);
//         assertSame(sm.currentState, SystemState.MAPPING);
//     }

//     @Test
//     void waitingOperation() throws Exception {
//         WaitingState ws = new WaitingState();
//         assertTrue(ws.performOperation());

//         db = new Database();
//         ws = new WaitingState();

//         assertFalse(ws.performOperation());
//         waitJimmy(System.currentTimeMillis(), 2000);
//         assertFalse(ws.performOperation());

//         db = new Database();
//         ws = new WaitingState();

//         assertFalse(ws.performOperation());
//         waitJimmy(System.currentTimeMillis(), 4500);
//         assertTrue(ws.performOperation());
//     }

//     @Test
//     void waitingOperation2() throws Exception {
//         WaitingState ws = new WaitingState();
//         db.fullPurge("BR01");

//         assertFalse(ws.performOperation());
//     }

//     @Test
//     void waitingOperation3() throws Exception {
//         db = new Database();

//         for (int i = 1; i < 6; i++) {
//             String id = "BR0" + i;
//             db.addClient(id, new BladeRunnerClient(id, new MessageGenerator(),
//                     new MessageSender(InetAddress.getLocalHost(), 3000 + i, id), 0));
//         }

//         for (int i = 1; i < 10; i++) {
//             String id = (i == 10) ? "CP" + i : "CP0" + i;
//             db.addClient(id, new org.example.client.CheckpointClient(id, new MessageGenerator(),
//                     new MessageSender(InetAddress.getLocalHost(), 4000 + i, id), i, 0));
//         }

//         WaitingState ws = new WaitingState();
//         assertFalse(ws.performOperation());

//     }

//     @Test
//     void runningOperation() throws Exception {
//         org.example.state.RunningState rs = new org.example.state.RunningState();

//         assertFalse(rs.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);
//         assertFalse(rs.performOperation());

//         waitJimmy(System.currentTimeMillis(), 2500);
//         assertFalse(rs.performOperation());

//         waitJimmy(System.currentTimeMillis(), 2500);
//         assertFalse(rs.performOperation());

//         waitJimmy(System.currentTimeMillis(), 2500);
//         assertFalse(rs.performOperation());

//         assertFalse(rs.performOperation());
//         assertFalse(rs.performOperation());
//         assertFalse(rs.performOperation());
//         assertFalse(rs.performOperation());

//         waitJimmy(System.currentTimeMillis(), 2500);
//         assertFalse(rs.performOperation());

//         waitJimmy(System.currentTimeMillis(), 2500);
//         assertFalse(rs.performOperation());
//     }

//     @Test
//     void runningOperation2() throws Exception {
//         org.example.state.RunningState rs = new org.example.state.RunningState();

//         assertFalse(rs.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);

//         assertFalse(rs.performOperation());
//         waitJimmy(System.currentTimeMillis(), 2500);
//         assertFalse(rs.performOperation());

//         assertSame(rs.getNextState(), SystemState.EMERGENCY);
//     }


//     @Test
//     void mapping() throws Exception {
//         org.example.state.MappingState ms = new org.example.state.MappingState(eb);

//         assertFalse(ms.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);
//         assertFalse(ms.performOperation());

//         assertFalse(ms.performOperation());
//         ms.addTrip(1, false);
//         ms.addTrip(1, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(2, false);
//         ms.addTrip(2, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(3, false);
//         ms.addTrip(3, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(4, false);
//         ms.addTrip(4, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(5, false);
//         ms.addTrip(5, true);
//         assertTrue(ms.performOperation());

//     }

//     @Test
//     void mapping1() throws Exception {
//         org.example.state.MappingState ms = new org.example.state.MappingState(eb);

//         assertFalse(ms.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);
//         assertFalse(ms.performOperation());

//         assertFalse(ms.performOperation());
//         ms.addTrip(1, false);
//         ms.addTrip(1, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(2, false);
//         ms.addTrip(2, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(3, false);
//         ms.addTrip(3, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(4, false);
//         ms.addTrip(4, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(5, false);
//         ms.addTrip(5, true);
//         assertTrue(ms.performOperation());
//     }

//     @Test
//     void mapping2() throws Exception {
//         org.example.state.MappingState ms = new org.example.state.MappingState(eb);

//         assertFalse(ms.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);

//         assertTrue(ms.performOperation());
//     }

//     @Test
//     void mapping3() throws Exception {
//         org.example.state.MappingState ms = new org.example.state.MappingState(eb);

//         assertFalse(ms.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);
//         assertFalse(ms.performOperation());

//         assertFalse(ms.performOperation());
//         ms.addTrip(10, false);
//         ms.addTrip(10, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(6, false);
//         ms.addTrip(6, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(3, false);
//         ms.addTrip(3, true);
//         assertFalse(ms.performOperation());

//         ms.addTrip(4, false);
//         ms.addTrip(4, true);
//         assertFalse(ms.performOperation());
//         assertFalse(ms.performOperation());

//         waitJimmy(System.currentTimeMillis(), 16000);

//         assertTrue(ms.performOperation());

//         waitJimmy(System.currentTimeMillis(), 16000);

//         assertTrue(ms.performOperation());

//         waitJimmy(System.currentTimeMillis(), 16000);

//         assertTrue(ms.performOperation());
//         waitJimmy(System.currentTimeMillis(), 16000);

//         assertTrue(ms.performOperation());

//     }

//     @Test
//     void mapping4() throws Exception {
//         org.example.state.MappingState ms = new org.example.state.MappingState(eb);

//         assertFalse(ms.performOperation());
//         waitJimmy(System.currentTimeMillis(), 3500);

//         ms.addTrip(-10, false);
//         ms.addTrip(-10, true);

//         assertTrue(ms.performOperation());
//     }


//     // MAPPING STATE, may need to run process twice before going to the next br

//     private void waitJimmy(long start, long sleep) {
//         while (System.currentTimeMillis() - start <= sleep);
//     }
// }