package test.validation.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import status.Sync;
import status.Status;
import task.sync.SyncTask;

import static org.junit.jupiter.api.Assertions.*;

public class SyncTaskValidationTest {
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
    void testConstructorInitialization() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_FREQUENCY, task.getFrequency());
        assertEquals(INITIAL_BUFFER_SIZE, task.getBufferSize());
        assertEquals(INITIAL_MODE, task.getMode());
        assertEquals(1, task.getLatency());
        assertEquals(1.0, task.getVoltageScale());
        assertEquals(Status.PENDING, task.getStatus());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0})
    @DisplayName("Frequency should not be less than 1.0")
    void testInvalidFrequency(double invalidFreq) {
        task.setFrequency(invalidFreq);
        assertEquals(1.0, task.getFrequency());
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 100.0, 1000.0, 2000.0})
    @DisplayName("Valid frequency values should be accepted")
    void testValidFrequency(double validFreq) {
        task.setFrequency(validFreq);
        assertEquals(validFreq, task.getFrequency());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
    @DisplayName("Buffer size should not be less than 1")
    void testInvalidBufferSize(int invalidSize) {
        task.setBufferSize(invalidSize);
        assertEquals(1, task.getBufferSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 64, 128, 256})
    @DisplayName("Valid buffer sizes should be accepted")
    void testValidBufferSize(int validSize) {
        task.setBufferSize(validSize);
        assertEquals(validSize, task.getBufferSize());
    }

    @Test
    @DisplayName("Buffer size should respect maximum limit")
    void testBufferSizeLimit() {
        task.setBufferSize(257);
        assertEquals(256, task.getBufferSize());
    }

    @ParameterizedTest
    @EnumSource(Sync.class)
    @DisplayName("Each sync mode should affect execution time correctly")
    void testModeFactor(Sync mode) {
        SyncTask asyncTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, Sync.ASYNC);
        long asyncTime = asyncTask.getExecutionTime();

        SyncTask testTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, mode);
        long modeTime = testTask.getExecutionTime();

        double expectedFactor = switch(mode) {
            case BARRIER -> 2.0;
            case PIPELINE -> 1.2;
            case WAVEFRONT -> 1.5;
            case ASYNC -> 1.0;
        };

        assertEquals(asyncTime * expectedFactor, modeTime, asyncTime * 0.01);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Latency should not be less than 1")
    void testInvalidLatency(int invalidLatency) {
        task.setLatency(invalidLatency);
        assertEquals(1, task.getLatency());
    }

    @Test
    @DisplayName("Latency should respect maximum limit")
    void testLatencyLimit() {
        task.setLatency(10001); // Above MAX_LATENCY (10000)
        assertEquals(10000, task.getLatency());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, 2.1, 3.0})
    @DisplayName("Voltage scale should be constrained between 0.1 and 2.0")
    void testInvalidVoltageScale(double invalidScale) {
        task.setVoltageScale(invalidScale);
        double scale = task.getVoltageScale();
        assertTrue(scale >= 0.1 && scale <= 2.0);
    }

    @ParameterizedTest
    @CsvSource({
            "500.0, 32, BARRIER",
            "1000.0, 64, PIPELINE",
            "2000.0, 128, WAVEFRONT",
            "4000.0, 256, ASYNC"
    })
    @DisplayName("Execution time should consider all factors")
    void testExecutionTimeFactors(double frequency, int bufferSize, Sync mode) {
        SyncTask testTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, frequency, bufferSize, mode);
        assertTrue(testTask.getExecutionTime() > 0);
    }
}