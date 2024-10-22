// import org.example.Database;
// import org.example.Processor;
// import org.example.client.*;
// import org.example.messages.MessageSender;
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

// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Logger;

// import java.lang.reflect.Field;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;



// class DatabaseTest {

// private static Database db;
// private static Field instanceFieldDB;

// @BeforeAll
// static void setUp() {
// db = Database.getInstanceTest();

// try {
// instanceFieldDB = Database.class.getDeclaredField("instance");
// instanceFieldDB.setAccessible(true);
// } catch (Exception e) {

// }

// }

// @Test
// void testTheInstanceWorks() {
// db.addClient("BR01", new BladeRunnerClient("BR01", new MessageGenerator(),
// new MessageSender(null, null, 0, null)));
// BladeRunnerClient c = db.getClient("BR01", BladeRunnerClient.class).get();

// assertNotNull(c);

// try {
// instanceFieldDB.set(null, null);
// db = Database.getInstanceTest();
// } catch (Exception e) {
// System.out.println("hello");
// }

// Optional<BladeRunnerClient> c2 = db.getClient("BR01", BladeRunnerClient.class);

// assertEquals(c2, Optional.empty());
// }
// }
