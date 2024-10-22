import org.example.Database;
import org.example.client.*;
import org.example.events.BladeRunnerStopEvent;
import org.example.events.ClientErrorEvent;
import org.example.events.EventBus;
import org.example.messages.*;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;



class MessageHandlerTest {
    @Mock
    Logger l;

    @Mock
    EventBus eb;

    @Mock
    Database db;

    @InjectMocks
    MessageHandler mh;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AutoCloseable closeable;


    @BeforeEach
    void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testMessage() {
        ReceiveMessage message = new ReceiveMessage();
        message.clientType = "CCP";
        message.clientID = "BR01";
        message.message = "AKEX";
        message.sequenceNumber = 3;

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, null, 3001);

        mh.handleMessage(sendPacket);

        verify(db, times(1)).getClient("BR01", BladeRunnerClient.class);
        verify(br, times(1)).expectingAKEXBy(3);
        verify(br, times(1)).isMissedAKEX(3);
    }

    @Test
    void testStationAndCheckpoint() {
        ReceiveMessage message = new ReceiveMessage();
        message.clientType = "CPC";
        message.clientID = "CP01";
        message.message = "AKEX";
        message.sequenceNumber = 3;

        CheckpointClient cp = mock(CheckpointClient.class);

        when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, null, 3001);

        mh.handleMessage(sendPacket);


        verify(db, times(1)).getClient("CP01", CheckpointClient.class);
        verify(cp, times(1)).setLastResponse("AKEX");
        verify(cp, times(1)).expectingAKEXBy(3);
        verify(cp, times(1)).isMissedAKEX(3);

    }

    @Test
    public void Times100test() {

        Integer scale = 100;
        ReceiveMessage r1 = new ReceiveMessage();
        r1.clientType = "CCP";
        r1.clientID = "BR01";
        r1.message = "STAT";
        r1.sequenceNumber = 4;
        r1.status = "FFASTC";

        ReceiveMessage r2 = new ReceiveMessage();
        r2.clientType = "CCP";
        r2.clientID = "BR01";
        r2.message = "STAT";
        r2.sequenceNumber = 5;
        r2.status = "STOPC";

        BladeRunnerClient br = mock(BladeRunnerClient.class);
        // Create a spy on the enum instance
        // SystemState spyStatus = Mockito.spy(SystemState.RUNNING);

        // Override the behavior of isSuccess() method


        for (int i = 0; i < scale; i++) {
            when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
            when(br.isExpectingStat()).thenReturn(true);
            when(br.getLatestStatusMessageCount()).thenReturn(3);
            // Mockito.when(spyStatus.equals(SystemState.RUNNING)).thenReturn(true);
            when(br.getId()).thenReturn("BR01");
            when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC); // This does not matter

            mh.handleStatMessage(br, r1);

            when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
            when(br.isExpectingStat()).thenReturn(false);
            when(br.getLatestStatusMessageCount()).thenReturn(4);
            // Mockito.when(spyStatus.equals(SystemState.RUNNING)).thenReturn(true);
            when(br.getId()).thenReturn("BR01");
            when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC); // This does not matter

            mh.handleStatMessage(br, r2);
        }

        verify(br, times(1 * scale)).sendAcknowledgeMessage(any(MessageEnums.AKType.class));
        verify(eb, times(1 * scale)).publish(any(BladeRunnerStopEvent.class));
        verify(br, times(1 * scale)).updateLatestStatusMessageCount(eq(4));
        verify(br, times(1 * scale)).updateLatestStatusMessageCount(eq(5));
        verify(br, times(2 * scale)).resetMissedStats();

        verify(br, times(2 * scale)).noLongerExpectingStat();
        verify(br, times(2 * scale)).updateStatus(eq(MessageEnums.CCPStatus.FFASTC));

        verify(l, times(2 * scale)).log(eq(Level.INFO), isA(String.class), isA(Object.class));
    }
}
