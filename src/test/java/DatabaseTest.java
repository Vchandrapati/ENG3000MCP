import org.example.Database;
import org.example.client.BladeRunnerClient;
import org.example.client.CheckpointClient;
import org.example.client.ReasonEnum;
import org.example.client.StationClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseTest {

    private static Database db;

    @BeforeEach
    void setUp () {
        db = Database.getInstance();
    }

    @AfterEach()
    void tearDown () {
        db.ultraPurge();
    }

    @Test
    void testAddAndGetClient () {
        BladeRunnerClient br = new BladeRunnerClient("BR01", null, null, 0);
        db.addClient("BR01", br);

        Optional<BladeRunnerClient> result = db.getClient("BR01", BladeRunnerClient.class);
        assertNotNull(result);
        assertEquals(br, result.get());
    }

    @Test
    void testAddDupeClient() {
        BladeRunnerClient bladeRunner = new BladeRunnerClient("BR01", null, null, 0);
        db.addClient("BR01", bladeRunner);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            db.addClient("BR01", bladeRunner);
        });

        assertEquals("Attempted to add duplicate client with id: BR01", exception.getMessage());
    }

    @Test
    void testGetNonExistentClient() {
        Optional<BladeRunnerClient> result = db.getClient("BR01", BladeRunnerClient.class);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetStationIfExist() {
        StationClient station = new StationClient("ST01", null, null, 0, 0);
        db.addClient("ST01", station);

        Optional<StationClient> result = db.getStationIfExist(0);
        assertTrue(result.isPresent());
        assertEquals("ST01", result.get().getId());
    }

    @Test
    void testGetStationIfNotExist() {
        Optional<StationClient> result = db.getStationIfExist(0);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddAndGetCCP () {
        CheckpointClient br = new CheckpointClient("CP01", null, null, 0, 0);
        db.addClient("CP01", br);

        Optional<CheckpointClient> result = db.getClient("CP01", CheckpointClient.class);
        assertNotNull(result);
        assertEquals(br, result.get());
    }

    @Test
    void testGetClientWrongClass () {
        CheckpointClient br = new CheckpointClient("CP01", null, null, 0, 0);
        db.addClient("CP01", br);

        Optional<BladeRunnerClient> result = db.getClient("CP01", BladeRunnerClient.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCheckpointCount() {
        CheckpointClient checkpoint = new CheckpointClient("CP01", null, null, 0, 0);
        db.addClient("CP01", checkpoint);

        assertEquals(1, db.getCheckpointCount());
    }

    @Test
    void testGetStationCount() {
        StationClient station = new StationClient("ST01", null, null, 0, 0);
        db.addClient("ST01", station);

        assertEquals(1, db.getStationCount());
    }

    @Test
    void testGetBladeRunnerCount() {
        BladeRunnerClient br = new BladeRunnerClient("BR01", null, null, 0);
        BladeRunnerClient br2 = new BladeRunnerClient("BR02", null, null, 0);
        db.addClient("BR01", br);
        db.addClient("BR02", br2);

        assertEquals(2, db.getBladeRunnerCount());
    }

    @Test
    void testFullPurge() {
        BladeRunnerClient br = new BladeRunnerClient("BR01", null, null, 0);
        db.addClient("BR01", br);
        db.addUnresponsiveClient("BR01", ReasonEnum.CLIENTERR);

        db.fullPurge("BR01");

        assertFalse(db.isClientUnresponsive("BR01"));
        assertEquals(Optional.empty(), db.getClient("BR01", BladeRunnerClient.class));
    }

    @Test
    void testUnresponsiveEmpty() {
        assertTrue(db.isUnresponsiveEmpty());

        BladeRunnerClient br = new BladeRunnerClient("BR01", null, null, 0);
        db.addClient("BR01", br);
        db.addUnresponsiveClient("BR01", ReasonEnum.CLIENTERR);

        assertFalse(db.isUnresponsiveEmpty());

        assertEquals(1, db.getAllUnresponsiveClientIDs().size());
    }

    @Test
    void testAddUnresponsiveNonExistentClient() {
        assertFalse(db.addUnresponsiveClient("BR01", ReasonEnum.CLIENTERR));
    }

    @Test
    void testGetClientReasonsWithNonExistentClient() {
        BladeRunnerClient br = new BladeRunnerClient("BR01", null, null, 0);
        db.addClient(br.getId(), br);
        assertEquals(new HashSet<>(), db.getClientReasons(br.getId()));
    }

    @Test
    void testGetBladeRunnerClients() {
        BladeRunnerClient br = new BladeRunnerClient("BR01", null, null, 0);
        db.addClient(br.getId(), br);

        assertEquals(1, db.getBladeRunnerClients().size());
    }
}
