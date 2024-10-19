import org.example.Database;
import org.example.messages.ClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;
import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



class ClientFactoryTest {

    @Mock
    Database db;

    @Mock
    HashMap<String, Integer> loc;

    @Mock
    Logger l;

    @InjectMocks
    ClientFactory cf;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_read() throws Exception {
        Scanner s = PowerMockito.mock(Scanner.class);


        when(s.hasNextLine()).thenReturn(true).thenReturn(true).thenReturn(false);

        File f = spy(new File(""));
        ClientFactory fileReaderService = spy(new ClientFactory());

        // Mock the Scanner constructor
        PowerMockito.whenNew(Scanner.class).withArguments(any(File.class)).thenReturn(s);

    }
}
