import org.example.Database;
import org.example.Processor;
import org.example.client.*;
import org.example.events.BladeRunnerStopEvent;
import org.example.events.ClientErrorEvent;
import org.example.events.ClientIntialiseEvent;
import org.example.events.EventBus;
import org.example.events.PacketEvent;
import org.example.events.StateChangeEvent;
import org.example.events.TripEvent;
import org.example.messages.*;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Event;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.InetAddress;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;



class MessageHandlerTest {
    @Mock
    Database db;

    @Mock
    Logger l;

    @Mock
    EventBus eb;

    @InjectMocks
    MessageHandler mh;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_Invalid_Message_Parse() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "AKEX";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = "coom" + objectMapper.writeValueAsString(messgae) + ".cfVGBHBHJKJCDEASWFWADXWA";
        } catch (Exception e) {
            System.out.println("HELLO");
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        mh.handleMessage(pe);

        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    void test_Unknown_Client_Parse() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "???";
        messgae.clientID = "BR01";
        messgae.message = "AKEX";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae) + ".";
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        mh.handleMessage(pe);

        verify(l, times(1)).log(eq(Level.WARNING), isA(String.class), eq("???"));
        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    // @Test
    // void test_Message_Handler_Error_Parse() {
    // ReceiveMessage messgae = new ReceiveMessage();
    // messgae.clientType = "CCP";
    // messgae.clientID = "BR01";
    // messgae.message = "AKEX";
    // messgae.sequenceNumber = 3;

    // String msg = "";
    // try {
    // msg = objectMapper.writeValueAsString(messgae) + ".";
    // } catch (Exception e) {
    // // TODO: handle exception
    // }

    // byte[] buffer = msg.getBytes();
    // DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
    // PacketEvent pe = new PacketEvent(packet);

    // mh.handleMessage(pe);

    // verify(l, times(1)).log(eq(Level.INFO), isA(String.class), isA(Object.class));
    // verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
    // verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
    // verifyNoMoreInteractions(eb);
    // }

    @Test
    void test_CCP_AKEX_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "AKEX";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("BR01"), eq(BladeRunnerClient.class));
        verify(br, times(1)).expectingAKEXBy(3);
        verify(br, times(1)).isMissedAKEX(3);
    }

    @Test
    void test_CCP_unknown_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "???";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("BR01"), eq(BladeRunnerClient.class));
        verify(br, times(1)).isMissedAKEX(3);
        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    void test_CCP_CCIN_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "CCIN";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.empty());

        mh.handleMessage(pe);

        verify(eb, times(1)).publish(isA(ClientIntialiseEvent.class));
    }

    @Test
    void test_NO_CCP_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "???";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.empty());

        mh.handleMessage(pe);

        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    void test_CCP_STAT_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "STOPC";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(eb).publish(isA(BladeRunnerStopEvent.class));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.CCPStatus.STOPC);
    }

    @Test
    void test_CCP_STAT_SLOW_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "STOPC";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(eb).publish(isA(BladeRunnerStopEvent.class));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.CCPStatus.STOPC);
    }

    @Test
    void test_CCP_STAT_IN_WAITING_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "STOPC";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.WAITING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.CCPStatus.STOPC);

        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    void test_CCP_STAT_FFASTC_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "FFASTC";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.CCPStatus.FFASTC);
    }

    @Test
    void test_CCP_STAT_Not_SLOW_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "FFASTC";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FFASTC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FFASTC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.CCPStatus.FFASTC);
    }



    @Test
    void test_CCP_STAT_Unknown_Status_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "?????";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(l, times(1)).log(isA(Level.class), isA(String.class), isA(Object.class));
    }

    @Test
    void test_CCP_STAT_ERR_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CCP";
        messgae.clientID = "BR01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "ERR";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        BladeRunnerClient br = mock(BladeRunnerClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);

        when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(eb).publish(isA(ClientErrorEvent.class));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.CCPStatus.ERR);
    }

    @Test
    public void test_CPC_AKEX_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CPC";
        messgae.clientID = "CP01";
        messgae.message = "AKEX";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        CheckpointClient br = mock(CheckpointClient.class);

        when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("CP01"), eq(CheckpointClient.class));
        verify(br, times(1)).expectingAKEXBy(3);
        verify(br, times(1)).isMissedAKEX(3);
    }

    @Test
    public void test_CPC_CPIN_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CPC";
        messgae.clientID = "CP01";
        messgae.message = "CPIN";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        CheckpointClient br = mock(CheckpointClient.class);

        when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.empty());

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("CP01"), eq(CheckpointClient.class));
        verify(eb, times(1)).publish(isA(ClientIntialiseEvent.class));
    }

    @Test
    public void test_NO_CPC_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "CPC";
        messgae.clientID = "CP01";
        messgae.message = "NONE";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        CheckpointClient br = mock(CheckpointClient.class);

        when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.empty());

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("CP01"), eq(CheckpointClient.class));

        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    public void test_STC_AKEX_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "AKEX";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("ST01"), eq(StationClient.class));
        verify(br, times(1)).expectingAKEXBy(3);
        verify(br, times(1)).isMissedAKEX(3);
    }

    @Test
    public void test_STC_STIN_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "STIN";
        messgae.sequenceNumber = 3;

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.empty());

        mh.handleMessage(pe);

        verify(eb, times(1)).publish(isA(ClientIntialiseEvent.class));
    }

    @Test
    public void test_STC_TRIP_ON_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "TRIP";
        messgae.sequenceNumber = 3;
        messgae.status = "ON";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("ST01"), eq(StationClient.class));
        verify(eb, times(1)).publish(isA(TripEvent.class));
        verify(br, times(1)).isMissedAKEX(3);
        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
    }

    @Test
    public void test_STC_TRIP_OFF_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "TRIP";
        messgae.sequenceNumber = 3;
        messgae.status = "OFF";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("ST01"), eq(StationClient.class));
        verify(eb, times(1)).publish(isA(TripEvent.class));
        verify(br, times(1)).isMissedAKEX(3);
        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
    }

    @Test
    public void test_STC_TRIP_ERR_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "TRIP";
        messgae.sequenceNumber = 3;
        messgae.status = "ERR";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("ST01"), eq(StationClient.class));
        verify(br, times(1)).isMissedAKEX(3);
        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
        verify(eb, times(1)).publish(isA(ClientErrorEvent.class));
    }

    @Test
    public void test_STC_NONE_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "NONE";
        messgae.sequenceNumber = 3;
        messgae.status = "ERR";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("ST01"), eq(StationClient.class));
        verify(br, times(1)).isMissedAKEX(3);

        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    public void test_STC_TRIP_NONE_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "TRIP";
        messgae.sequenceNumber = 3;
        messgae.status = "NONE";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(db, times(1)).getClient(eq("ST01"), eq(StationClient.class));
        verify(br, times(1)).isMissedAKEX(3);
        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    public void test_NO_STC_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "TRIP";
        messgae.sequenceNumber = 3;
        messgae.status = "NONE";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.empty());

        mh.handleMessage(pe);

        verify(eb, times(1)).subscribe(eq(StateChangeEvent.class), any());
        verify(eb, times(1)).subscribe(eq(PacketEvent.class), any());
        verifyNoMoreInteractions(eb);
    }

    @Test
    public void test_STC_STAT_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 3;
        messgae.status = "OFF";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(MessageEnums.STCAction.OFF);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(2);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.STCStatus.OFF);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br).isMissedAKEX(3);
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(br).updateLatestStatusMessageCount(3);
        verify(br).resetMissedStats();
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.STCStatus.OFF);
    }

    @Test
    public void test_STC_STAT_plus_Message() {
        ReceiveMessage messgae = new ReceiveMessage();
        messgae.clientType = "STC";
        messgae.clientID = "ST01";
        messgae.message = "STAT";
        messgae.sequenceNumber = 10;
        messgae.status = "OFF";

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(messgae);
        } catch (Exception e) {
            // TODO: handle exception
        }

        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
        PacketEvent pe = new PacketEvent(packet);

        StationClient br = mock(StationClient.class);

        try {
            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            // Create an instance of StateChangeEvent with the desired state
            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }


        when(br.getLastActionSent()).thenReturn(null);
        when(br.isExpectingStat()).thenReturn(false);
        when(br.getLatestStatusMessageCount()).thenReturn(11);
        when(br.getId()).thenReturn("BR01");
        when(br.getStatus()).thenReturn(MessageEnums.STCStatus.OFF);

        when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(br));

        mh.handleMessage(pe);

        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
        verify(br, never()).resetMissedStats();
        verify(br).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
        verify(br).noLongerExpectingStat();
        verify(br).updateStatus(MessageEnums.STCStatus.OFF);
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
        r2.status = "FFASTC";

        BladeRunnerClient br = mock(BladeRunnerClient.class);
        try {

            Method updateStateMethod = MessageHandler.class.getDeclaredMethod("updateCurrentState",
                    StateChangeEvent.class);
            updateStateMethod.setAccessible(true); // Allow access to the private method

            StateChangeEvent stateChangeEvent = new StateChangeEvent(SystemState.RUNNING);
            updateStateMethod.invoke(mh, stateChangeEvent);
        } catch (Exception e) {
            // TODO: handle exception
        }



        for (int i = 0; i < scale; i++) {
            when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
            when(br.isExpectingStat()).thenReturn(true);
            when(br.getLatestStatusMessageCount()).thenReturn(3);
            when(br.getId()).thenReturn("BR01");
            when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC); // This does not matter

            mh.handleStatMessage(br, r1);

            when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
            when(br.isExpectingStat()).thenReturn(false);
            when(br.getLatestStatusMessageCount()).thenReturn(4);
            when(br.getId()).thenReturn("BR01");
            when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC); // This does not matter

            mh.handleStatMessage(br, r2);
        }

        verify(br, times(1 * scale)).sendAcknowledgeMessage(any(MessageEnums.AKType.class));

        verify(br, times(1 * scale)).updateLatestStatusMessageCount(eq(4));
        verify(br, times(1 * scale)).updateLatestStatusMessageCount(eq(5));
        verify(br, times(2 * scale)).resetMissedStats();

        verify(br, times(2 * scale)).noLongerExpectingStat();
        verify(br, times(2 * scale)).updateStatus(eq(MessageEnums.CCPStatus.FFASTC));

        verify(l, times(2 * scale)).log(eq(Level.INFO), isA(String.class), isA(Object.class));
    }
}
