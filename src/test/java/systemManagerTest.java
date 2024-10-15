import org.example.Database;
import org.example.Processor;
import org.example.client.AbstractClient;
import org.example.client.ReasonEnum;
import org.example.messages.StatHandler;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class systemManagerTest {

    @Mock
    private Database mockDatabase;

    @Mock
    private SystemStateManager mockSystemStateManager;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Ensure that singletons (if needed) return mocked instances
        when(Database.getInstance()).thenReturn(mockDatabase);
        when(SystemStateManager.getInstance()).thenReturn(mockSystemStateManager);
    }

    @Test
    void sysManagerAddUnresponsive() {
        mockSystemStateManager.addUnresponsiveClient(null, null);
        assertTrue(mockSystemStateManager.error);

    }

    @Test
    void sysManagerNeedstrip() {
        mockSystemStateManager.needsTrip(0, false);
    }

    @Test
    void sysManagersetState() {
        mockSystemStateManager.setState(null);
    }

    @Test
    void sysManagerRun() {
        mockSystemStateManager.run();
    }

    @Test
    void sysManagergetInstance() {
        mockSystemStateManager.getInstance();
    }
}
