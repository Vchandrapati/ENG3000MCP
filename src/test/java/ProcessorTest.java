<<<<<<< HEAD
// import org.example.client.*;

// import java.util.Optional;
// import java.util.logging.*;

// import static org.mockito.Mockito.*;
// import static org.junit.jupiter.api.Assertions.*;
// import org.example.Database;
// import org.example.Processor;
// import org.example.state.SystemStateManager;
// import org.example.messages.MessageEnums;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.ArrayList;
// import java.util.List;
=======
import org.example.client.*;
import org.example.events.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.*;

import org.example.state.RunningState;
import org.example.state.SystemState;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.example.Database;
import org.example.Processor;
import org.example.messages.MessageEnums;
import org.example.state.SystemStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.logging.Level;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.List;
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// class ProcessorTest {

<<<<<<< HEAD
// private Database mockDb;
// private BladeRunnerClient mockBladeRunnerClient;
// private SystemStateManager mockSystemStateManager;
// private Processor mockProcessor;
// private MessageGenerator mockMessageGenerator;
// private Processor processor;
// private TestLogHandler logHandler; // Custom log handler to capture log messages
// private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


// @BeforeEach
// void setUp() {
// // Mocking the Database and BladeRunnerClient
// mockDb = mock(Database.class);
// mockSystemStateManager = mock(SystemStateManager.class);
// mockMessageGenerator = mock(MessageGenerator.class);
// mockBladeRunnerClient = mock(BladeRunnerClient.class);

// // Set up the Processor with the mocked Database
// processor = new Processor(mockDb, mockSystemStateManager);
=======
    private Database mockDb;
    private BladeRunnerClient mockBladeRunnerClient;
    private SystemStateManager mockSystemStateManager;
    private Processor mockProcessor;
    private MessageGenerator mockMessageGenerator;
    private EventBus mockEventBus;
    private Processor processor;
    private TestLogHandler logHandler;  // Custom log handler to capture log messages
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    TripEvent tripEventTripped;
    TripEvent tripEventUntripped;


    @BeforeEach
    void setUp() {
        // Mocking the Database and BladeRunnerClient
        mockDb = mock(Database.class);
        mockSystemStateManager = mock(SystemStateManager.class);
        mockMessageGenerator = mock(MessageGenerator.class);
        mockBladeRunnerClient = mock(BladeRunnerClient.class);
        mockEventBus = mock(EventBus.getInstance().getClass());
        tripEventTripped = new TripEvent(3, false);
        tripEventUntripped = new TripEvent(3, true);

        // Set up the Processor with the mocked Database
        processor = new Processor(mockEventBus, mockDb);
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// // Set up log handler to capture log messages
// logHandler = new TestLogHandler(); // Assuming LogHandler is the custom handler
// logger.addHandler(logHandler); // Add custom handler to logger
// logger.setLevel(Level.ALL);


// }


    ArgumentCaptor<ClientErrorEvent> checkpointTrippedAccess(int checkpointTripped, boolean untrip, SystemState state) throws Exception {
        // Use reflection to access the private checkpointTripped method
        Method method = Processor.class.getDeclaredMethod("checkpointTripped", TripEvent.class);
        method.setAccessible(true);  // Allow access to the private method

        // Create the TripEvent instance to invoke the method
        TripEvent tripEvent = new TripEvent(checkpointTripped, untrip);

        // Invoke the method using reflection
        method.invoke(processor, tripEvent);

        // Use reflection to access the private updateState method
        Method updateStateMethod = Processor.class.getDeclaredMethod("updateState", StateChangeEvent.class);
        updateStateMethod.setAccessible(true);  // Allow access to the private method

        // Create an instance of StateChangeEvent with the desired state
        StateChangeEvent stateChangeEvent = new StateChangeEvent(state);
        updateStateMethod.invoke(processor, stateChangeEvent);

        // Prepare the captor for capturing the ClientErrorEvent
        ArgumentCaptor<ClientErrorEvent> captor = ArgumentCaptor.forClass(ClientErrorEvent.class);

        // Now verify that the publish method was called on the eventBus
        verify(mockEventBus).publish(captor.capture()); // Capture the published event

        return captor; // Return the captor containing the captured event
    }

    ArgumentCaptor<ClientErrorEvent> handleTripAccess(int checkpointTripped, int previousBlock, boolean untrip) throws Exception {
        // Use reflection to access the private handleTrip method
        Method method = Processor.class.getDeclaredMethod("handleTrip", int.class, int.class, boolean.class);
        method.setAccessible(true);  // Allow access to the private method

        // Invoke the method using reflection
        method.invoke(processor, checkpointTripped, previousBlock, untrip);

        // Prepare the captor for capturing the ClientErrorEvent
        ArgumentCaptor<ClientErrorEvent> captor = ArgumentCaptor.forClass(ClientErrorEvent.class);

        // Verify that the publish method was called on the eventBus
        verify(mockEventBus).publish(captor.capture()); // Capture the published event

        return captor; // Return the captor containing the captured event
    }

    ArgumentCaptor<ClientErrorEvent> reverseTripAccess(BladeRunnerClient reversingBladeRunner, int checkpointTripped, boolean untrip) throws Exception {
        // Use reflection to access the private reverseTrip method
        Method method = Processor.class.getDeclaredMethod("reverseTrip", BladeRunnerClient.class, int.class, boolean.class);
        method.setAccessible(true);  // Allow access to the private method

        // Invoke the method using reflection
        method.invoke(processor, reversingBladeRunner, checkpointTripped, untrip);

        // Prepare the captor for capturing the ClientErrorEvent
        ArgumentCaptor<ClientErrorEvent> captor = ArgumentCaptor.forClass(ClientErrorEvent.class);

        // Verify that the publish method was called on the eventBus
        verify(mockEventBus).publish(captor.capture()); // Capture the published event

        return captor; // Return the captor containing the captured event
    }

    ArgumentCaptor<BladeRunnerStopEvent> bladeRunnerStoppedAccess(String id) throws Exception {
        // Use reflection to access the private bladeRunnerStopped method
        Method method = Processor.class.getDeclaredMethod("bladeRunnerStopped", BladeRunnerStopEvent.class);
        method.setAccessible(true);  // Allow access to the private method

        // Create a BladeRunnerStopEvent and invoke the method
        BladeRunnerStopEvent event = new BladeRunnerStopEvent(id);
        method.invoke(processor, event);

        // Capture the event to verify later
        ArgumentCaptor<BladeRunnerStopEvent> captor = ArgumentCaptor.forClass(BladeRunnerStopEvent.class);
        verify(mockEventBus).publish(captor.capture()); // Verify that the publish method was called

        return captor; // Return the captor containing the captured event
    }


<<<<<<< HEAD
// //CHECKPOINT TRIPPED 100% coverage
// @Test
// void testCheckPointTrippedInvalidTrip(){
// BladeRunnerClient br = new BladeRunnerClient("BR01",null,null, 0);

// //System.out.println(br.getId());
// when(mockDb.getBlockCount()).thenReturn(10);

// //isnextblockvalid, return false
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.empty());
// when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.empty());

// // Mocking the client for BR01
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

// processor.checkpointTripped(3, true);


// assertTrue(logHandler.containsLog(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip
// boolean : {1}"));
=======
    //CHECKPOINT TRIPPED 100% coverage
    @Test
    void testCheckPointTrippedInvalidTrip() throws Exception {
        ArgumentCaptor<ClientErrorEvent> captor = checkpointTrippedAccess(3, true, SystemState.RUNNING);

        verify(mockEventBus).publish(captor.capture());

        // Extract the captured argument and assert
        ClientErrorEvent capturedEvent = captor.getValue();
        assertEquals("CP03", capturedEvent.getId());
        assertEquals(ReasonEnum.INCORTRIP, capturedEvent.getReason());

>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// }

<<<<<<< HEAD
// @Test
// void testCheckPointTrippedvalidTrip(){
// BladeRunnerClient br = new BladeRunnerClient("BR01",null,null, 0);

// //System.out.println(br.getId());
// when(mockDb.getBlockCount()).thenReturn(10);

// //isnextblockvalid, return false
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.empty());
// when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.empty());

// // Mocking the client for BR01
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

// processor.checkpointTripped(3, true);


// assertTrue(logHandler.containsLog(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip
// boolean : {1}"));

// }

// @Test
// void testCheckPointTrippedNeedsTrip(){
// CheckpointClient cc = mock(CheckpointClient.class);

// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

// when(mockSystemStateManager.needsTrip(3, true)).thenReturn(true);

// processor.checkpointTripped(3, true);

// assertTrue(logHandler.containsLog(Level.WARNING, "Sent to mapping state"));
=======

    @Test
    void testCheckPointTrippedBladeRunnerMissing() throws Exception {
        CheckpointClient cc = mock(CheckpointClient.class);

        // Mock the database behavior
        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        // Mock the BladeRunnerClient retrieval, returns empty to enter
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.empty());


        ArgumentCaptor<ClientErrorEvent> captor = checkpointTrippedAccess(3, true, SystemState.RUNNING);

        // Verify that the publish method was called
        verify(mockEventBus).publish(captor.capture());


        // Extract the captured argument and assert its properties
        ClientErrorEvent capturedEvent = captor.getValue();
        assertEquals("CP03", capturedEvent.getId());
        assertEquals(ReasonEnum.INCORTRIP, capturedEvent.getReason());

>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// }

<<<<<<< HEAD
// @Test
// void testCheckPointTrippedReversing() {
// CheckpointClient cc = mock(CheckpointClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// //BladeRunnerClient br = new BladeRunnerClient("BR01",mockMessageGenerator, null);


// when(mockDb.getBlockCount()).thenReturn(10);

// // Mock behavior for Checkpoint and Station Clients
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

// when(mockSystemStateManager.needsTrip(3, true)).thenReturn(false);

// // Mocking the client for BR01
// when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn("BR01");
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

// // Set the status of the BladeRunnerClient to RSLOWC
// when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
// when(mockDb.isBlockOccupied(3)).thenReturn(true);

// // Call the method under test
// processor.checkpointTripped(3, true);

// assertTrue(logHandler.containsLog(Level.FINEST, "Reversing reached"));
// }

// @Test
// void testCheckPointTrippedForwardButBladeRunnerMissing() {
// CheckpointClient cc = mock(CheckpointClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// //BladeRunnerClient br = new BladeRunnerClient("BR01",mockMessageGenerator, null);


// when(mockDb.getBlockCount()).thenReturn(10);

// // Mock behavior for Checkpoint and Station Clients
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

// when(mockSystemStateManager.needsTrip(3, true)).thenReturn(false);

// // Mocking the client for BR01
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

// when(mockDb.isBlockOccupied(2)).thenReturn(false);

// // Call the method under test
// processor.checkpointTripped(3, true);

// assertTrue(logHandler.containsLog(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip
// boolean : {1}"));
// }

// @Test
// void testCheckPointTrippedForward() {
// CheckpointClient cc = mock(CheckpointClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// //BladeRunnerClient br = new BladeRunnerClient("BR01",mockMessageGenerator, null);


// when(mockDb.getBlockCount()).thenReturn(10);

// // Mock behavior for Checkpoint and Station Clients
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

// when(mockSystemStateManager.needsTrip(3, true)).thenReturn(false);

// // Mocking the client for BR01
// when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn("BR01");
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

// // Set the status of the BladeRunnerClient to RSLOWC
// when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FFASTC);
// when(mockDb.isBlockOccupied(3)).thenReturn(true);

// // Call the method under test
// processor.checkpointTripped(3, true);

// assertTrue(logHandler.containsLog(Level.FINEST, "forward reached"));
// }



// //handleTrip

// @Test
// void testHandleTripEmptyBR(){
// when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn(null);

// processor.handleTrip(3, 2, true);
// assertTrue(logHandler.containsLog(Level.WARNING, "Tried to get blade runner at checkpoint {0} but
// failed"));
// }

// @Test
// void testHandleTripTrippedBlockFull(){
// CheckpointClient cc = mock(CheckpointClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
// when(mockDb.isBlockOccupied(3)).thenReturn(true);

// processor.handleTrip(3, 2, true);

// assertTrue(logHandler.containsLog(Level.WARNING, "Multiple blade runners in the same zone,
// includes : {0}"));
// }

// @Test
// void testHandleTripCheckpointStation(){
// CheckpointClient cc = mock(CheckpointClient.class);
// StationClient st = mock(StationClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
// when(mockDb.isBlockOccupied(3)).thenReturn(false);

=======

    @Test
    void testCheckPointTrippedSendToMapping() throws Exception {
        // Arrange
        CheckpointClient cc = mock(CheckpointClient.class);

        // Mock the database behavior
        when(mockDb.getBlockCount()).thenReturn(10);

        //isNextBlockValid returns false
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));


        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.empty());


        when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.empty());


        ArgumentCaptor<ClientErrorEvent> captor = checkpointTrippedAccess(3, true, SystemState.MAPPING);


        assertTrue(processor.isMappingStateTriggered());

    }


    @Test
    void testCheckPointTrippedReversing() throws Exception {
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);

        // Mock the database behavior
        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));


        when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));


        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);
        when(mockDb.isBlockOccupied(3)).thenReturn(true);


        ArgumentCaptor<ClientErrorEvent> captor = checkpointTrippedAccess(3, true, SystemState.RUNNING);

        // Verify that the event was published, no need to assert for an INCORTRIP since we're in the reversing logic
        assertTrue(logHandler.containsLog(Level.FINEST, "Reversing reached"));

    }

    @Test
    void testCheckPointTrippedForward() throws Exception {
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);

        // Mock the database behavior
        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

        // Mocking the client for BR01
        when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));

        // Set the status of the BladeRunnerClient to FFASTC
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FFASTC);
        when(mockDb.isBlockOccupied(3)).thenReturn(true);

        // Call the method using the checkpointTrippedAccess helper method
        ArgumentCaptor<ClientErrorEvent> captor = checkpointTrippedAccess(3, true, SystemState.RUNNING);

        // Verify that the event bus publish was called
        verify(mockEventBus).publish(captor.capture());

        // Optionally assert the log contains the expected message
        assertTrue(logHandler.containsLog(Level.FINEST, "forward reached"));

    }


//handleTrip

    @Test
    void testHandleTripEmptyBR() throws Exception {
        // Mock the behavior to return no BladeRunner in block 3
        when(mockDb.getLastBladeRunnerInBlock(3)).thenReturn(null);

        // Call the method using the appropriate access method
        ArgumentCaptor<ClientErrorEvent> captor = handleTripAccess(3, 2, true);

        // Verify that the publish method was called
        verify(mockEventBus).publish(captor.capture());

        // Assert that the log contains the expected warning
        assertTrue(logHandler.containsLog(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed"));

        // Additional assertions on the captured event if necessary
        ClientErrorEvent capturedEvent = captor.getValue();
        // Add assertions specific to the captured event here
    }

    @Test
    void testHandleTripTrippedBlockFull() throws Exception {
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        when(br.getId()).thenReturn("BR01");

        // Mock the database behavior
        when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.isBlockOccupied(3)).thenReturn(true);

        // Call the method using the appropriate access method
        ArgumentCaptor<ClientErrorEvent> captor = handleTripAccess(3, 2, true);

        // Verify that the publish method was called
        verify(mockEventBus).publish(captor.capture());

        // Assert that the log contains the expected warning
        //assertTrue(logHandler.containsLog(Level.WARNING, "Multiple blade runners in the same zone, includes : {0}"));

        // Additional assertions on the captured event if necessary
        ClientErrorEvent capturedEvent = captor.getValue();
        // Add assertions specific to the captured event here

        // Assert properties of the captured event
        assertEquals("BR01", capturedEvent.getId()); // Assuming you want to check the first Blade Runner
        assertEquals(ReasonEnum.INCORTRIP, capturedEvent.getReason());
    }


    @Test
    void testHandleTripCheckpointStation() {
        CheckpointClient cc = mock(CheckpointClient.class);
        StationClient st = mock(StationClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.isBlockOccupied(3)).thenReturn(false);
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// when(mockDb.getBlockCount()).thenReturn(10);
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

// when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));

// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));

// when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));

<<<<<<< HEAD
// when(mockBladeRunnerClient.isDockedAtStation()).thenReturn(false);
// processor.handleTrip(3, 2, true);

// verify(br).sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
// }
=======
        when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));
        when(mockDb.getStationIfExist(4)).thenReturn(Optional.of(st));
        when(mockBladeRunnerClient.isDockedAtStation()).thenReturn(false);

        processor.handleTrip(3, 2, true);
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// @Test
// void testHandleTripNextBlockOccupied(){
// CheckpointClient cc = mock(CheckpointClient.class);
// StationClient st = mock(StationClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
// when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
// when(mockDb.isBlockOccupied(3)).thenReturn(false);

<<<<<<< HEAD
// when(mockDb.getBlockCount()).thenReturn(10);

// when(mockDb.isBlockOccupied(4)).thenReturn(true);
// when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));
// processor.handleTrip(3, 2, true);

// verify(br).sendExecuteMessage(MessageEnums.CCPAction.STOPC);
// }



// // reverse Trip

// @Test
// void testReverseTripBadReverse(){
// StationClient sc = mock(StationClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);

// when(mockDb.getStationIfExist(3)).thenReturn(Optional.empty());
// when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.RSLOWC);

// processor.reverseTrip(br, 3, false);
=======

    @Test
    void testHandleTripNextBlockOccupied() throws Exception {
        CheckpointClient cc = mock(CheckpointClient.class);
        StationClient st = mock(StationClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);

        when(mockDb.getLastBladeRunnerInBlock(2)).thenReturn("BR01");
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.isBlockOccupied(3)).thenReturn(false);
        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.isBlockOccupied(4)).thenReturn(true);
        when(mockDb.getClient("ST04", StationClient.class)).thenReturn(Optional.of(st));

        // Call the handleTripAccess method instead of processor.handleTrip
        ArgumentCaptor<ClientErrorEvent> captor = handleTripAccess(3, 2, true);

        // Verify that the publish method was called
        verify(mockEventBus).publish(captor.capture());

        // Extract the captured argument and assert its properties
        ClientErrorEvent capturedEvent = captor.getValue();
        assertEquals("BR01", capturedEvent.getId()); // Ensure the expected ID is captured
        assertEquals(ReasonEnum.INCORTRIP, capturedEvent.getReason());
    }

// Reverse Trip Tests

    @Test
    void testReverseTripBadReverse() throws Exception {
        BladeRunnerClient br = mock(BladeRunnerClient.class);
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// verify(br).sendExecuteMessage(MessageEnums.CCPAction.FFASTC);

<<<<<<< HEAD
// }

// @Test
// void testReverseTripBRInPreviousBlock(){ //NEED TO FIX, DOESNT WORK YET
// StationClient sc = mock(StationClient.class);
// CheckpointClient cc = mock(CheckpointClient.class);
// BladeRunnerClient br = mock(BladeRunnerClient.class);
// Optional<StationClient> sc2 = Optional.of(mock(StationClient.class));

// when(mockDb.getStationIfExist(3)).thenReturn(Optional.of(sc));
// when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FFASTC);
// when(mockDb.getBlockCount()).thenReturn(10);
// when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));
// when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.of(sc));

// when(mockDb.isBlockOccupied(2)).thenReturn(true);

// processor.reverseTrip(br, 3, false);
// verify(br).sendExecuteMessage(MessageEnums.CCPAction.STOPC);

// }



// // You can add more tests for success cases as well.
=======

        ArgumentCaptor<ClientErrorEvent> captor = reverseTripAccess(br, 3, false);


        verify(mockEventBus).publish(captor.capture());

        assertTrue(logHandler.containsLog(Level.WARNING, "blade Runner reversing when it shouldn't"));
    }


    @Test
    void testReverseTripBRInPreviousBlock() throws Exception {
        StationClient sc = mock(StationClient.class);
        CheckpointClient cc = mock(CheckpointClient.class);
        BladeRunnerClient br = mock(BladeRunnerClient.class);


        when(mockDb.getStationIfExist(3)).thenReturn(Optional.of(sc));
        when(br.getStatus()).thenReturn(MessageEnums.CCPStatus.FFASTC);
        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.getClient("CP03", CheckpointClient.class)).thenReturn(Optional.of(cc));
        when(mockDb.getClient("ST03", StationClient.class)).thenReturn(Optional.of(sc));
        when(mockDb.isBlockOccupied(2)).thenReturn(true);
        when(mockDb.getClient("CP02", CheckpointClient.class)).thenReturn(Optional.of(cc));

        ArgumentCaptor<ClientErrorEvent> captor = reverseTripAccess(br, 3, false);

        verify(mockEventBus).publish(captor.capture());

        assertTrue(logHandler.containsLog(Level.WARNING, "blade Runner reversing but previous block occupied"));
    }


    //bladeRunner stopped

    @Test
    void testBladeRunnerStoppedWithPresentClient() throws Exception {
        BladeRunnerClient br = mock(BladeRunnerClient.class);
        StationClient sc = mock(StationClient.class);

        // Mocking necessary database interactions
        when(mockDb.getClient("BR01", BladeRunnerClient.class)).thenReturn(Optional.of(br));
        when(mockDb.getStationIfExist(1)).thenReturn(Optional.of(sc)); // Assuming calculateNextBlock returns 1 for this example
        when(mockDb.getBlockCount()).thenReturn(10);
        when(mockDb.getClient("STA02", StationClient.class)).thenReturn(Optional.of(sc));
        when(br.getZone()).thenReturn(1); // Adjust the return value as necessary
        when(mockDb.getStationIfExist(2)).thenReturn(Optional.of(sc));
        when(sc.getId()).thenReturn("STA02");
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// public class TestLogHandler extends java.util.logging.Handler {
// private List<LogRecord> logRecords = new ArrayList<>();

// @Override
// public void publish(LogRecord record) {
// logRecords.add(record);
// }

<<<<<<< HEAD
// @Override
// public void flush() {
// }
=======
        ArgumentCaptor<BladeRunnerStopEvent> captor = bladeRunnerStoppedAccess("BR01");
>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6

// @Override
// public void close() throws SecurityException {
// }

<<<<<<< HEAD
// public boolean containsLog(Level level, String message) {
// return logRecords.stream().anyMatch(log ->
// log.getLevel().equals(level) && log.getMessage().contains(message));
// }
// public List<String> getCapturedLogs() {
// List<String> messages = new ArrayList<>();
// for (LogRecord record : logRecords) {
// messages.add(record.getMessage());
// }
// return messages;
// }
// }
// }
=======
        verify(br).sendExecuteMessage(MessageEnums.CCPAction.STOPO);
        verify(br).updateStatus(MessageEnums.CCPStatus.STOPO);


        verify(sc).sendExecuteMessage(MessageEnums.STCAction.OPEN);
        verify(sc).updateStatus(MessageEnums.STCStatus.ONOPEN);
    }

    @Test
    void testBladeRunnerStoppedWithAbsentClient() throws Exception {
        String id = "BR02";

        when(mockDb.getClient(id, BladeRunnerClient.class)).thenReturn(Optional.empty());


        ArgumentCaptor<BladeRunnerStopEvent> captor = bladeRunnerStoppedAccess(id);


        verify(mockDb, times(1)).getClient(id, BladeRunnerClient.class);
        verify(mockEventBus, never()).publish(any());
    }


    class TestLogHandler extends Handler {
        private final CopyOnWriteArrayList<String> logMessages = new CopyOnWriteArrayList<>();

        @Override
        public void publish(LogRecord record) {
            logMessages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public List<String> getLogs() {
            return logMessages;
        }

        public boolean containsLog(Level level, String message) {
            return logMessages.stream()
                    .anyMatch(log -> log.contains(level.getName()) && log.contains(message));
        }
    }
}

>>>>>>> d7252bf48c45153b21d70522f64d8e16fc72cbb6
