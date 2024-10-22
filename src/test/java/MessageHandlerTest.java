// import org.example.Database;
// import org.example.client.*;
// import org.example.messages.*;
// import org.example.state.SystemStateManager;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import com.fasterxml.jackson.databind.ObjectMapper;
//
// import java.util.Optional;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.net.InetAddress;
//
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.ArgumentMatchers.isA;
// import static org.mockito.Mockito.*;
//
//
//
// class MessageHandlerTest {
//
// @Mock
// StatHandler sh;
//
// @Mock
// Database db;
//
// @Mock
// SystemStateManager sm;
//
// @Mock
// ClientFactory cf;
//
// @Mock
// Logger l;
//
// @InjectMocks
// MessageHandler mh;
//
// private ObjectMapper objectMapper = new ObjectMapper();
//
// @BeforeEach
// void beforeEach() {
// MockitoAnnotations.openMocks(this);
//
// }
//
// @Test
// void test_CCP_STAT_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "BR01";
// r.clientType = "CCP";
// r.message = "STAT";
// r.sequenceNumber = 0;
//
// BladeRunnerClient br = mock(BladeRunnerClient.class);
//
// when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(sh, times(1)).handleStatMessage(eq(br), isA(ReceiveMessage.class));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CCP_AKEX_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "BR01";
// r.clientType = "CCP";
// r.message = "AKEX";
// r.sequenceNumber = 0;
//
// BladeRunnerClient br = mock(BladeRunnerClient.class);
//
// when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CCP_CCIN_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "BR01";
// r.clientType = "CCP";
// r.message = "CCIN";
// r.sequenceNumber = 0;
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(cf, times(1)).handleInitialise(isA(ReceiveMessage.class),
// eq(InetAddress.getLocalHost()), eq(0));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_unknown_CCP_ERR_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "BR01";
// r.clientType = "CCP";
// r.message = "ERR";
// r.sequenceNumber = 0;
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(l, times(1)).log(eq(Level.SEVERE), isA(String.class), eq("BR01"));
// }
//
// @Test
// void test_known_CCP_ERR_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "BR01";
// r.clientType = "CCP";
// r.message = "ERR";
// r.sequenceNumber = 0;
//
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// when(db.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(l, times(1)).log(eq(Level.WARNING), isA(String.class), eq(r.message));
// }
//
// @Test
// void test_CPC_AKEX_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "AKEX";
// r.sequenceNumber = 0;
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CPC_STAT_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "STAT";
// r.sequenceNumber = 0;
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(sh, times(1)).handleStatMessage(eq(cp), isA(ReceiveMessage.class));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CPC_CPIN_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "CPIN";
// r.sequenceNumber = 0;
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(cf, times(1)).handleInitialise(isA(ReceiveMessage.class),
// eq(InetAddress.getLocalHost()), eq(0));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CPC_TRIP_ON_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "TRIP";
// r.sequenceNumber = 0;
// r.status = "ON";
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));
// when(cp.getLocation()).thenReturn(0);
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(cp).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
// verify(cp, times(1)).updateStatus(eq(MessageEnums.CPCStatus.ON));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CPC_TRIP_OFF_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "TRIP";
// r.sequenceNumber = 0;
// r.status = "OFF";
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));
// when(cp.getLocation()).thenReturn(0);
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(cp).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
// verify(cp, times(1)).updateStatus(eq(MessageEnums.CPCStatus.OFF));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_CPC_TRIP_ERR_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "TRIP";
// r.sequenceNumber = 0;
// r.status = "ERR";
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));
// when(cp.getId()).thenReturn("CP01");
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(cp).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
// verify(sm, times(1)).addUnresponsiveClient(eq("CP01"), eq(ReasonEnum.CLIENTERR));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_unkown_CPC_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "???";
// r.sequenceNumber = 0;
// r.status = "ERR";
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(l, times(1)).log(eq(Level.SEVERE), isA(String.class), eq("CP01"));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_kown_CPC_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "CP01";
// r.clientType = "CPC";
// r.message = "???";
// r.sequenceNumber = 0;
// r.status = "ERR";
//
// CheckpointClient cp = mock(CheckpointClient.class);
//
// when(db.getClient("CP01", CheckpointClient.class)).thenReturn(Optional.of(cp));
// when(cp.getId()).thenReturn("CP01");
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(l, times(1)).log(eq(Level.SEVERE), isA(String.class), isA(ReceiveMessage.class));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_STAT_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "STAT";
// r.sequenceNumber = 0;
//
// StationClient st = mock(StationClient.class);
//
// when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(st));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(sh, times(1)).handleStatMessage(eq(st), isA(ReceiveMessage.class));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_AKEX_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "AKEX";
// r.sequenceNumber = 0;
//
// StationClient st = mock(StationClient.class);
//
// when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(st));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_STIN_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "STIN";
// r.sequenceNumber = 0;
//
// StationClient st = mock(StationClient.class);
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(cf).handleInitialise(isA(ReceiveMessage.class), eq(InetAddress.getLocalHost()),
// eq(0));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_TRIP_ON_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "TRIP";
// r.sequenceNumber = 0;
// r.status = "ON";
//
// StationClient st = mock(StationClient.class);
//
// when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(st));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(st).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
// verify(st).updateStatus(eq(MessageEnums.STCStatus.ON));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_TRIP_OFF_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "TRIP";
// r.sequenceNumber = 0;
// r.status = "OFF";
//
// StationClient st = mock(StationClient.class);
//
// when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(st));
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(st).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
// verify(st).updateStatus(eq(MessageEnums.STCStatus.OFF));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_TRIP_ERR_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "TRIP";
// r.sequenceNumber = 0;
// r.status = "ERR";
//
// StationClient st = mock(StationClient.class);
//
// when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(st));
// when(st.getId()).thenReturn("ST01");
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(st).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKTR));
// verify(sm).addUnresponsiveClient(eq("ST01"), eq(ReasonEnum.CLIENTERR));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_known_ERR_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "???";
// r.sequenceNumber = 0;
//
// StationClient st = mock(StationClient.class);
//
// when(db.getClient("ST01", StationClient.class)).thenReturn(Optional.of(st));
// when(st.getId()).thenReturn("ST01");
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
// verify(l).log(eq(Level.WARNING), isA(String.class), eq(r.message));
// verifyNoMoreInteractions(sh);
// }
//
// @Test
// void test_STC_unknown_ERR_MSG() throws Exception {
// ReceiveMessage r = new ReceiveMessage();
// r.clientID = "ST01";
// r.clientType = "STC";
// r.message = "???";
// r.sequenceNumber = 0;
//
// String s = objectMapper.writeValueAsString(r);
// mh.handleMessage(s, InetAddress.getLocalHost(), 0);
//
//
// verify(l).log(eq(Level.SEVERE), isA(String.class), eq("ST01"));
// verifyNoMoreInteractions(sh);
// }
// }
