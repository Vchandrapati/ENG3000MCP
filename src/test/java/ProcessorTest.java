import org.example.client.*;
import org.example.messages.MessageSender;
import org.example.state.SystemStateInterface;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.util.Optional;
import java.util.logging.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.example.Database;
import org.example.Processor;
import org.example.messages.StatHandler;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;
import org.example.messages.MessageEnums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProcessorTest {

    private Database mockDb;
    private BladeRunnerClient mockBladeRunnerClient;
    private SystemStateManager mockSystemStateManager;
    private Processor mockProcessor;
    private MessageGenerator mockMessageGenerator;
    private Processor processor;
    private TestLogHandler logHandler;  // Custom log handler to capture log messages
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    @BeforeEach
    void setUp() {
        // Mocking the Database and BladeRunnerClient
        mockDb = mock(Database.class);
        mockSystemStateManager = mock(SystemStateManager.class);
        mockMessageGenerator = mock(MessageGenerator.class);
        mockBladeRunnerClient = mock(BladeRunnerClient.class);

        // Set up the Processor with the mocked Database
        processor = new Processor(mockDb, mockSystemStateManager);

        // Set up log handler to capture log messages
        logHandler = new TestLogHandler();  // Assuming LogHandler is the custom handler
        logger.addHandler(logHandler);  // Add custom handler to logger
        logger.setLevel(Level.ALL);


    }



    //CHECKPOINT TRIPPED 100% coverage
    @Test
    void testCheckPointTrippedInvalidTrip(){
        BladeRunnerClient br = new BladeRunnerClient("BR01",null,null);

        //System.out.println(br.getId());
        when(mockDb.getBlockCount()).thenReturn(10);

        //isnextblockvalid, return false
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.empty());
        when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.empty());

        // Mocking the client for BR01
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        processor.checkpointTripped(3, true);


        assertTrue(logHandler.containsLog(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}"));

    }

    @Test
    void testCheckPointTrippedvalidTrip(){
        BladeRunnerClient br = new BladeRunnerClient("BR01",null,null);

        //System.out.println(br.getId());
        when(mockDb.getBlockCount()).thenReturn(10);

        //isnextblockvalid, return false
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.empty());
        when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.empty());

        // Mocking the client for BR01
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        processor.checkpointTripped(3, true);


        assertTrue(logHandler.containsLog(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}"));

    }

    @Test
    void testCheckPointTrippedNeedsTrip(){
        CheckpointClient cc = mock(CheckpointClient.class);

        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        when(mockSystemStateManager.needsTrip(3, true)).thenReturn(true);

        processor.checkpointTripped(3, true);

        assertTrue(logHandler.containsLog(Level.WARNING, "Sent to mapping state"));

    }

    @Test
    void testCheckPointTrippedReversing() {
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        //BladeRunnerClient br = new BladeRunnerClient("BR01",mockMessageGenerator, null);


        when(mockDb.getBlockCount()).thenReturn(10);

        // Mock behavior for Checkpoint and Station Clients
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        when(mockSystemStateManager.needsTrip(3, true)).thenReturn(false);

        // Mocking the client for BR01
        when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        // Set the status of the BladeRunnerClient to RSLOWC
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
        when(mockDb.isBlockOccupied(3)).thenReturn(true);

        // Call the method under test
        processor.checkpointTripped(3, true);

        assertTrue(logHandler.containsLog(Level.FINEST, "Reversing reached"));
    }

    @Test
    void testCheckPointTrippedForwardButBladeRunnerMissing() {
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        //BladeRunnerClient br = new BladeRunnerClient("BR01",mockMessageGenerator, null);


        when(mockDb.getBlockCount()).thenReturn(10);

        // Mock behavior for Checkpoint and Station Clients
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        when(mockSystemStateManager.needsTrip(3, true)).thenReturn(false);

        // Mocking the client for BR01
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        when(mockDb.isBlockOccupied(2)).thenReturn(false);

        // Call the method under test
        processor.checkpointTripped(3, true);

        assertTrue(logHandler.containsLog(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}"));
    }

    @Test
    void testCheckPointTrippedForward() {
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        //BladeRunnerClient br = new BladeRunnerClient("BR01",mockMessageGenerator, null);


        when(mockDb.getBlockCount()).thenReturn(10);

        // Mock behavior for Checkpoint and Station Clients
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        when(mockSystemStateManager.needsTrip(3, true)).thenReturn(false);

        // Mocking the client for BR01
        when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        // Set the status of the BladeRunnerClient to RSLOWC
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FFASTC);
        when(mockDb.isBlockOccupied(3)).thenReturn(true);

        // Call the method under test
        processor.checkpointTripped(3, true);

        assertTrue(logHandler.containsLog(Level.FINEST, "forward reached"));
    }




    //handleTrip

    @Test
    void testHandleTripEmptyBR(){
        when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn(null);

        processor.handleTrip(3, 2, true);
        assertTrue(logHandler.containsLog(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed"));
    }

    @Test
    void testHandleTripTrippedBlockFull(){
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.isBlockOccupied(3)).thenReturn(true);

        processor.handleTrip(3, 2, true);

        assertTrue(logHandler.containsLog(Level.WARNING, "Multiple blade runners in the same zone, includes : {0}"));
    }

    @Test
    void testHandleTripCheckpointStation(){
        CheckpointClient cc = mock(CheckpointClient.class);
        StationClient st = mock(StationClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.isBlockOccupied(3)).thenReturn(false);


        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));

        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));

        when(mockBladeRunnerClient.isDockedAtStation()).thenReturn(false);
        processor.handleTrip(3, 2, true);

        verify(br).sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
    }

    @Test
    void testHandleTripNextBlockOccupied(){
        CheckpointClient cc = mock(CheckpointClient.class);
        StationClient st = mock(StationClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.isBlockOccupied(3)).thenReturn(false);

        when(mockDb.getBlockCount()).thenReturn(10);

        when(mockDb.isBlockOccupied(4)).thenReturn(true);
        when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));
        processor.handleTrip(3, 2, true);

        verify(br).sendExecuteMessage(MessageEnums.CCPAction.STOPC);
    }








    // You can add more tests for success cases as well.

    public class TestLogHandler extends java.util.logging.Handler {
        private List<LogRecord> logRecords = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            logRecords.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean containsLog(Level level, String message) {
            return logRecords.stream().anyMatch(log ->
                    log.getLevel().equals(level) && log.getMessage().contains(message));
        }
        public List<String> getCapturedLogs() {
            List<String> messages = new ArrayList<>();
            for (LogRecord record : logRecords) {
                messages.add(record.getMessage());
            }
            return messages;
        }
    }
}
