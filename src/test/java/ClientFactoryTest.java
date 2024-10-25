//import org.example.Database;
//import org.example.client.AbstractClient;
//import org.example.client.BladeRunnerClient;
//import org.example.events.ClientErrorEvent;
//import org.example.events.ClientIntialiseEvent;
//import org.example.events.EventBus;
//import org.example.messages.ClientFactory;
//import org.example.messages.MessageEnums;
//import org.example.messages.ReceiveMessage;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.powermock.api.mockito.PowerMockito;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.HashMap;
//import java.util.Scanner;
//import java.util.logging.Logger;
//import java.io.File;
//import java.net.DatagramPacket;
//import java.net.InetAddress;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.isA;
//import static org.mockito.Mockito.*;
//
//
//
//class ClientFactoryTest {
//
//    @Mock
//    Database db;
//
//    @Mock
//    HashMap<String, Integer> loc;
//
//    @Mock
//    Logger l;
//
//    @Mock
//    EventBus eb;
//
//    @InjectMocks
//    ClientFactory cf;
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void test_CCP_locations() {
//
//        ReceiveMessage messgae = new ReceiveMessage();
//        messgae.clientType = "CCP";
//        messgae.clientID = "BR01";
//        messgae.message = "CCIN";
//        messgae.sequenceNumber = 3;
//
//        String msg = "";
//        try {
//            msg = objectMapper.writeValueAsString(messgae) + ".";
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        byte[] buffer = msg.getBytes();
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
//
//        ClientIntialiseEvent cie = new ClientIntialiseEvent(messgae, null, 0);
//
//        when(loc.get(isA(String.class))).thenReturn(1);
//
//        cf.handleInitialise(cie);
//
//        verify(db, times(1)).addClient(eq("BR01"), isA(AbstractClient.class));
//        verify(eb, times(1)).publish(isA(ClientErrorEvent.class));
//    }
//
//    @Test
//    void test_CPC_locations() {
//
//        ReceiveMessage messgae = new ReceiveMessage();
//        messgae.clientType = "CPC";
//        messgae.clientID = "CP01";
//        messgae.message = "CCIN";
//        messgae.sequenceNumber = 3;
//
//        String msg = "";
//        try {
//            msg = objectMapper.writeValueAsString(messgae) + ".";
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        byte[] buffer = msg.getBytes();
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
//
//        ClientIntialiseEvent cie = new ClientIntialiseEvent(messgae, null, 0);
//        try {
//            cie = new ClientIntialiseEvent(messgae, InetAddress.getLocalHost(), 6969);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        when(loc.get(any(String.class))).thenReturn(1);
//
//        cf.handleInitialise(cie);
//
//        verify(db, times(1)).addClient(eq("CP01"), isA(AbstractClient.class));
//        verify(eb, times(1)).publish(isA(ClientErrorEvent.class));
//    }
//
//    @Test
//    void test_STC_locations() {
//
//        ReceiveMessage messgae = new ReceiveMessage();
//        messgae.clientType = "STC";
//        messgae.clientID = "ST01";
//        messgae.message = "CCIN";
//        messgae.sequenceNumber = 3;
//
//        String msg = "";
//        try {
//            msg = objectMapper.writeValueAsString(messgae) + ".";
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        byte[] buffer = msg.getBytes();
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
//
//        ClientIntialiseEvent cie = new ClientIntialiseEvent(messgae, null, 0);
//        try {
//            cie = new ClientIntialiseEvent(messgae, InetAddress.getLocalHost(), 6969);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//        when(loc.get(isA(String.class))).thenReturn(1);
//
//        cf.handleInitialise(cie);
//
//        verify(db, times(1)).addClient(eq("ST01"), isA(AbstractClient.class));
//        verify(eb, times(1)).publish(isA(ClientErrorEvent.class));
//    }
//
//    @Test
//    void test_none_locations() {
//
//        ReceiveMessage messgae = new ReceiveMessage();
//        messgae.clientType = "????";
//        messgae.clientID = "ST01";
//        messgae.message = "CCIN";
//        messgae.sequenceNumber = 3;
//
//        String msg = "";
//        try {
//            msg = objectMapper.writeValueAsString(messgae) + ".";
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        byte[] buffer = msg.getBytes();
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
//
//        ClientIntialiseEvent cie = new ClientIntialiseEvent(messgae, null, 0);
//        try {
//            cie = new ClientIntialiseEvent(messgae, InetAddress.getLocalHost(), 6969);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//        when(loc.get(isA(String.class))).thenReturn(1);
//
//        cf.handleInitialise(cie);
//
//        verifyNoInteractions(db);
//        verify(eb, times(1)).subscribe(eq(ClientIntialiseEvent.class), any());
//        verifyNoMoreInteractions(eb);
//    }
//
//    @Test
//    void test_Exception_locations() {
//
//        ReceiveMessage messgae = new ReceiveMessage();
//        messgae.clientType = "STC";
//        messgae.clientID = "ST01";
//        messgae.message = "STIN";
//        messgae.sequenceNumber = 3;
//
//        String msg = "";
//        try {
//            msg = objectMapper.writeValueAsString(messgae) + ".";
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        byte[] buffer = msg.getBytes();
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, null, 69);
//
//        ClientIntialiseEvent cie = new ClientIntialiseEvent(null, null, 0);
//
//        when(loc.get(isA(String.class))).thenReturn(null);
//
//        cf.handleInitialise(cie);
//
//        verifyNoInteractions(db);
//        verify(eb, times(1)).subscribe(eq(ClientIntialiseEvent.class), any());
//        verifyNoMoreInteractions(eb);
//    }
//}
//
