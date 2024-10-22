// import org.example.Database;
// import org.example.Processor;
// import org.example.client.AbstractClient;
// import org.example.client.BladeRunnerClient;
// import org.example.client.MessageGenerator;
// import org.example.client.ReasonEnum;
// import org.example.client.MessageSender;
// import org.example.messages.Server;
// import org.example.messages.StatHandler;
// import org.example.state.SystemState;
// import org.example.state.SystemStateManager;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import java.lang.reflect.Field;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
// import java.util.logging.Logger;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// class systemManagerTest {

// private static Database db;

// private static SystemStateManager sm;

// private static Field instanceFieldSM;
// private static Field instanceFieldDB;

// private static List<String> ids;
// private static List<ReasonEnum> reasons;

// @BeforeAll
// static void setupList() {
// db = Database.getInstanceTest();
// sm = SystemStateManager.getInstance();
// ids = new ArrayList<>();
// reasons = new ArrayList<>();

// try {
// instanceFieldSM = SystemStateManager.class.getDeclaredField("instance");
// instanceFieldSM.setAccessible(true);

// instanceFieldDB = Database.class.getDeclaredField("instance");
// instanceFieldDB.setAccessible(true);
// }
// catch (Exception e) {

// }

// ids.add(null); reasons.add(null);
// ids.add(""); reasons.add(null);
// ids.add("what?"); reasons.add(null);
// ids.add(null); reasons.add(ReasonEnum.COLLISION);
// ids.add("waht2?"); reasons.add(ReasonEnum.COLLISION);
// ids.add("BR01"); reasons.add(ReasonEnum.COLLISION);
// ids.add("BR01"); reasons.add(ReasonEnum.NOSTAT);
// //7
// }

// @BeforeEach
// void setUp() throws Exception {
// instanceFieldDB.set(null, null);
// db = Database.getInstanceTest();

// // Set the field to null, effectively resetting the singleton
// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// }

// @Test
// void sysManagerAddUnresponsiveInWaiting() throws Exception {
// assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();

// assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();

// assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();

// assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();

// assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();

// assertFalse(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();

// assertFalse(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);
// }

// @Test
// void sysManagerAddUnresponsiveInMapping() throws Exception {
// sm.setState(SystemState.MAPPING);
// db.addClient("BR01", new BladeRunnerClient("BR01", new MessageGenerator(), new
// MessageSender(null, null, 0, null)));
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.MAPPING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.MAPPING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.MAPPING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.MAPPING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.MAPPING);
// sm.injectDatabase(db);

// assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);

// assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);


// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.MAPPING);
// sm.injectDatabase(db);

// assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);

// assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);
// }

// @Test
// void sysManagerAddUnresponsiveInRunning() throws Exception {
// sm.setState(SystemState.RUNNING);
// db.addClient("BR01", new BladeRunnerClient("BR01", new MessageGenerator(), new
// MessageSender(null, null, 0, null)));
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
// assertFalse(sm.error);
// assertNotSame(sm.getState(), SystemState.EMERGENCY);


// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
// assertTrue(sm.error);
// Thread.sleep(1000);
// assertSame(SystemState.EMERGENCY, sm.getState());


// assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);


// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);


// assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
// assertTrue(sm.error);
// assertSame(sm.getState(), SystemState.EMERGENCY);

// }

// @Test
// void sysManagerAddUnresponsiveInEmergency() throws Exception {
// sm.setState(SystemState.EMERGENCY);
// db.addClient("BR01", new BladeRunnerClient("BR01", new MessageGenerator(), new
// MessageSender(null, null, 0, null)));
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(0), reasons.get(0)));
// assertFalse(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(1), reasons.get(1)));
// assertFalse(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(2), reasons.get(2)));
// assertFalse(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(3), reasons.get(3)));
// assertFalse(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertFalse(sm.addUnresponsiveClient(ids.get(4), reasons.get(4)));
// assertFalse(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertTrue(sm.addUnresponsiveClient(ids.get(5), reasons.get(5)));
// assertTrue(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// sm.setState(SystemState.RUNNING);
// sm.injectDatabase(db);

// assertTrue(sm.addUnresponsiveClient(ids.get(6), reasons.get(6)));
// assertTrue(sm.error);
// }



// @Test
// void sysManagerNeedstrip() throws Exception {
// assertFalse(sm.needsTrip(0, false));
// assertFalse(sm.error);

// instanceFieldSM.set(null, null);
// sm = SystemStateManager.getInstance();
// }


// @Test
// void sysManagersetState() throws Exception {
// sm.setState(null);
// }

// @Test
// void sysManagerRun() throws Exception {
// sm.run();
// }

// @Test
// void sysManagergetInstance() throws Exception {
// sm.getInstance();
// }
// }
