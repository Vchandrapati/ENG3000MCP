//import org.example.Database;
//import org.example.Processor;
//import org.example.client.AbstractClient;
//import org.example.client.BladeRunnerClient;
//import org.example.client.ReasonEnum;
//import org.example.messages.MessageEnums;
//import org.example.messages.ReceiveMessage;
//import org.example.state.SystemState;
//import org.example.state.SystemStateManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//
//import static org.junit.Assert.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.isA;
//import static org.mockito.Mockito.*;
//
//import java.lang.reflect.*;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Processor.class)
//class StatHandlerTest {
//
//    @Mock
//    Database db;
//
//    @Mock
//    SystemStateManager sm;
//
//    @Mock
//    Logger l;
//
//    // @Mock
//    // ScheduledExecutorService ses;
//
//    @InjectMocks
//    StatHandler sh;
//
//    @BeforeEach
//    void beforeEach() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void test_Start_Status_1_MSG_Scheduler() throws Exception {
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        List<AbstractClient> clients = new ArrayList<AbstractClient>();
//        clients.add(br);
//        when(db.getClients()).thenReturn(clients);
//
//        sh.startStatusScheduler();
//
//        naptime(System.currentTimeMillis(), 1000);
//        verify(br, times(1)).sendStatusMessage();
//    }
//
//    @Test
//    void test_Start_Status_3_MSG_Scheduler() {
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        List<AbstractClient> clients = new ArrayList<AbstractClient>();
//        clients.add(br);
//        when(db.getClients()).thenReturn(clients);
//
//
//        sh.startStatusScheduler();
//        naptime(System.currentTimeMillis(), 5000);
//        verify(br, times(3)).sendStatusMessage();
//
//    }
//
//    @Test
//    void test_Start_Status_1_MultiClient_MSG_Scheduler() {
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//        BladeRunnerClient br2 = mock(BladeRunnerClient.class);
//
//        List<AbstractClient> clients = new ArrayList<AbstractClient>();
//        clients.add(br);
//        clients.add(br2);
//        when(db.getClients()).thenReturn(clients);
//
//        sh.startStatusScheduler();
//        naptime(System.currentTimeMillis(), 1000);
//        verify(br, times(1)).sendStatusMessage();
//        verify(br2, times(1)).sendStatusMessage();
//
//    }
//
//    @Test
//    void test_Start_Status_3_MultiClient_MSG_Scheduler() {
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//        BladeRunnerClient br2 = mock(BladeRunnerClient.class);
//
//        List<AbstractClient> clients = new ArrayList<AbstractClient>();
//        clients.add(br);
//        clients.add(br2);
//        when(db.getClients()).thenReturn(clients);
//
//
//        sh.startStatusScheduler();
//
//        naptime(System.currentTimeMillis(), 5000);
//        verify(br, times(3)).sendStatusMessage();
//        verify(br2, times(3)).sendStatusMessage();
//
//    }
//
//    @Test
//    void test_checkIfClientIsUnresponsive_true() throws Exception {
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.checkResponsive()).thenReturn(true);
//        when(br.getId()).thenReturn("BR01");
//
//        Method m = null;
//        try {
//            m = StatHandler.class.getDeclaredMethod("checkIfClientIsUnresponsive",
//                    AbstractClient.class);
//            m.setAccessible(true);
//
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        m.invoke(sh, br);
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.NOSTAT));
//    }
//
//    @Test
//    void test_checkIfClientIsUnresponsive_false() throws Exception {
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.checkResponsive()).thenReturn(false);
//        when(br.getId()).thenReturn("BR01");
//
//        Method m = null;
//        try {
//            m = StatHandler.class.getDeclaredMethod("checkIfClientIsUnresponsive",
//                    AbstractClient.class);
//            m.setAccessible(true);
//
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        m.invoke(sh, br);
//        verifyNoInteractions(sm);
//    }
//
//    // @Test
//    // void test_shutdown() {
//    // Field f;
//    // ScheduledExecutorService ses = mock(ScheduledExecutorService.class);
//    // try {
//    // f = StatHandler.class.getDeclaredField("scheduler");
//    // f.setAccessible(true);
//    // f.set(sh, ses);
//    // } catch (Exception e) {
//    // // TODO: handle exception
//    // }
//
//
//    // sh.shutdown();
//    // verify(ses, times(1)).shutdown();
//    // verifyNoMoreInteractions(sm);
//    // }
//
//    @Test
//    void test_ERR_noexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.STOPC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.STOPC);
//        when(br.isExpectingStat()).thenReturn(false);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_ERR_noexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.STOPC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.STOPC);
//        when(br.isExpectingStat()).thenReturn(false);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_ERR_yesexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.STOPC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.STOPC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_ERR_yesexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.STOPC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.STOPC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_FSLOWC_noexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(false);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_FSLOWC_yesexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_FSLOWC_noexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_FSLOWC_yesexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_RSLOWC_yesexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_RSLOWC_noexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
//        when(br.isExpectingStat()).thenReturn(false);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_RSLOWC_yesexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_STOPC_when_last_is_RSLOWC_noexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "STOPC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(isA(String.class), isA(ReasonEnum.class));
//
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, times(1));
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_ERR_noexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "ERR";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_differ_lastRSlow_noexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "FFASTC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.RSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.WRONGSTATUS));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_differ_lastFSlow_noexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "RSLOWC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.WRONGSTATUS));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_differ_lastFSlow_yesexp_noreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 2;
//        r.status = "RSLOWC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(false);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.WRONGSTATUS));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_differ_lastFSlow_yesexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "RSLOWC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(false);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, times(1)).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.WRONGSTATUS));
//
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_differ_lastFSlow_noexp_yesreset() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "RSLOWC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//        verify(sm, times(1)).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.WRONGSTATUS));
//
//        verify(br, times(1)).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, times(1)).resetMissedStats();
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    void test_CCP_fail() {
//        ReceiveMessage r = new ReceiveMessage();
//        r.clientType = "CCP";
//        r.clientID = "BR01";
//        r.message = "STAT";
//        r.sequenceNumber = 4;
//        r.status = "???";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC);
//        when(br.isExpectingStat()).thenReturn(true);
//        when(br.getLatestStatusMessageCount()).thenReturn(3);
//        when(sm.getState()).thenReturn(SystemState.RUNNING);
//        when(br.getId()).thenReturn("BR01");
//
//        sh.handleStatMessage(br, r);
//
//        verify(br, never()).sendAcknowledgeMessage(eq(MessageEnums.AKType.AKST));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.CLIENTERR));
//        verify(sm, never()).addUnresponsiveClient(eq("BR01"), eq(ReasonEnum.WRONGSTATUS));
//
//        verify(br, never()).updateLatestStatusMessageCount(isA(Integer.class));
//        verify(br, never()).resetMissedStats();
//
//        verify(l, times(1)).log(eq(Level.INFO), isA(String.class), isA(Object.class));
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//    @Test
//    public void Times100test() {
//
//        Integer scale = 100;
//        ReceiveMessage r1 = new ReceiveMessage();
//        r1.clientType = "CCP";
//        r1.clientID = "BR01";
//        r1.message = "STAT";
//        r1.sequenceNumber = 4;
//        r1.status = "FFASTC";
//
//        ReceiveMessage r2 = new ReceiveMessage();
//        r2.clientType = "CCP";
//        r2.clientID = "BR01";
//        r2.message = "STAT";
//        r2.sequenceNumber = 5;
//        r2.status = "FFASTC";
//
//        BladeRunnerClient br = mock(BladeRunnerClient.class);
//
//        for (int i = 0; i < scale; i++) {
//            when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//            when(br.isExpectingStat()).thenReturn(true);
//            when(br.getLatestStatusMessageCount()).thenReturn(3);
//            when(sm.getState()).thenReturn(SystemState.RUNNING);
//            when(br.getId()).thenReturn("BR01");
//            when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC); // This does not matter
//
//            sh.handleStatMessage(br, r1);
//
//            when(br.getLastActionSent()).thenReturn(MessageEnums.CCPAction.FSLOWC);
//            when(br.isExpectingStat()).thenReturn(false);
//            when(br.getLatestStatusMessageCount()).thenReturn(4);
//            when(sm.getState()).thenReturn(SystemState.RUNNING);
//            when(br.getId()).thenReturn("BR01");
//            when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FSLOWC); // This does not matter
//
//            sh.handleStatMessage(br, r2);
//        }
//
//        verify(br, times(1 * scale)).sendAcknowledgeMessage(any(MessageEnums.AKType.class));
//        verify(sm, never()).addUnresponsiveClient(any(String.class), any(ReasonEnum.class));
//
//        verify(br, times(1 * scale)).updateLatestStatusMessageCount(eq(4));
//        verify(br, times(1 * scale)).updateLatestStatusMessageCount(eq(5));
//        verify(br, times(2 * scale)).resetMissedStats();
//
//        verify(br, times(2 * scale)).noLongerExpectingStat();
//        verify(br, times(2 * scale)).updateStatus(eq(MessageEnums.CCPStatus.FFASTC));
//
//        verify(l, times(2 * scale)).log(eq(Level.INFO), isA(String.class), isA(Object.class));
//
//        PowerMockito.verifyStatic(Processor.class, never());
//        Processor.bladeRunnerStopped("BR01");
//    }
//
//
//    private void naptime(long start, long amount) {
//        while (System.currentTimeMillis() - start < amount) {
//        }
//    }
//}
