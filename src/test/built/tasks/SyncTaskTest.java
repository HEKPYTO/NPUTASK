package test.built.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import status.Sync;
import status.Status;
import task.sync.SyncTask;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SyncTaskTest {
    private SyncTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final double INITIAL_FREQUENCY = 1000.0;
    private static final int INITIAL_BUFFER_SIZE = 64;
    private static final Sync INITIAL_MODE = Sync.BARRIER;

    @BeforeEach
    void setUp() {
        task = new SyncTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, INITIAL_MODE);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructor() {
        assertNotNull(task);
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_FREQUENCY, task.getFrequency());
        assertEquals(INITIAL_BUFFER_SIZE, task.getBufferSize());
        assertEquals(INITIAL_MODE, task.getMode());
        assertEquals(1, task.getLatency());
        assertEquals(1.0, task.getVoltageScale());
        assertEquals(Status.PENDING, task.getStatus());
        assertTrue(task.getExecutionTime() > 0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0})
    @DisplayName("Frequency should not be less than 1.0")
    void testInvalidFrequency(double invalidFrequency) {
        task.setFrequency(invalidFrequency);
        assertEquals(1.0, task.getFrequency());
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 100.0, 1000.0, 2000.0})
    @DisplayName("Valid frequency values should be accepted")
    void testValidFrequency(double validFrequency) {
        task.setFrequency(validFrequency);
        assertEquals(validFrequency, task.getFrequency());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Buffer size should not be less than 1")
    void testInvalidBufferSize(int invalidSize) {
        task.setBufferSize(invalidSize);
        assertEquals(1, task.getBufferSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 16, 64, 128, 256})
    @DisplayName("Valid buffer sizes should be accepted")
    void testValidBufferSize(int validSize) {
        task.setBufferSize(validSize);
        assertEquals(validSize, task.getBufferSize());
    }

    @ParameterizedTest
    @EnumSource(Sync.class)
    @DisplayName("All sync modes should affect execution time correctly")
    void testSyncModeImpact() {
        SyncTask referenceTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, Sync.ASYNC);
        long baseTime = referenceTask.getExecutionTime();

        Map<Sync, Double> expectedFactors = Map.of(
                Sync.BARRIER, 2.0,
                Sync.PIPELINE, 1.2,
                Sync.WAVEFRONT, 1.5,
                Sync.ASYNC, 1.0
        );

        for (Sync mode : Sync.values()) {
            SyncTask testTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, mode);

            double expectedTime = baseTime * expectedFactors.get(mode);
            double actualTime = testTask.getExecutionTime();
            double tolerance = Math.abs(expectedTime * 0.01); // 1% tolerance

            assertEquals(expectedTime, actualTime, tolerance,
                    String.format("Mode %s: expected %.2f but got %.2f", mode, expectedTime, actualTime));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Latency should not be less than 1")
    void testInvalidLatency(int invalidLatency) {
        task.setLatency(invalidLatency);
        assertEquals(1, task.getLatency());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10})
    @DisplayName("Valid latency values should be accepted")
    void testValidLatency(int validLatency) {
        task.setLatency(validLatency);
        assertEquals(validLatency, task.getLatency());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -0.5, 2.1, 3.0})
    @DisplayName("Voltage scale should be constrained between 0.1 and 2.0")
    void testInvalidVoltageScale(double invalidScale) {
        task.setVoltageScale(invalidScale);
        double scale = task.getVoltageScale();
        assertTrue(scale >= 0.1 && scale <= 2.0,
                "Voltage scale should be between 0.1 and 2.0, but was: " + scale);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 0.5, 1.0, 1.5, 2.0})
    @DisplayName("Valid voltage scale values should be accepted")
    void testValidVoltageScale(double validScale) {
        task.setVoltageScale(validScale);
        assertEquals(validScale, task.getVoltageScale());
    }

    @Test
    @DisplayName("Execution time should consider all factors")
    void testCompleteExecutionTimeCalculation() {
        long initialTime = task.getExecutionTime();

        task.setFrequency(2000.0);
        assertTrue(task.getExecutionTime() < initialTime);

        task.setBufferSize(128);
        task.setLatency(4);
        task.setVoltageScale(0.5);

        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Mode should be immutable")
    void testModeImmutability() {
        assertEquals(INITIAL_MODE, task.getMode());
    }

    @Test
    @DisplayName("Execution time calculation should handle boundary values")
    void testExecutionTimeBoundaries() {
        SyncTask minTask = new SyncTask(TASK_ID, 100, 0,
                1.0, 1, Sync.ASYNC);
        assertTrue(minTask.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Task should maintain inherited status functionality")
    void testStatusInheritance() {
        assertEquals(Status.PENDING, task.getStatus());
        task.setStatus(Status.RUNNING);
        assertEquals(Status.RUNNING, task.getStatus());
    }
}