package test.built;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import status.Status;
import task.NPUTask;

import static org.junit.jupiter.api.Assertions.*;

class NPUTaskTest {
    private NPUTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;

    @BeforeEach
    void setUp() {
        task = new NPUTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructor() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(Status.PENDING, task.getStatus());
        assertEquals(0.0, task.getPowerConsumption());
        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Constructor should handle maximum long value")
    void testConstructorWithMaxLong() {
        NPUTask maxTask = new NPUTask(Long.MAX_VALUE, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
        assertEquals(Long.MAX_VALUE, maxTask.getTaskId());
    }

    @Test
    @DisplayName("Constructor should handle minimum long value")
    void testConstructorWithMinLong() {
        NPUTask minTask = new NPUTask(Long.MIN_VALUE, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
        assertEquals(Long.MIN_VALUE, minTask.getTaskId());
    }

    @Test
    @DisplayName("Execute should change status to RUNNING")
    void testExecuteStatusChange() {
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());
        assertTrue(task.getPowerConsumption() > 0);
    }

    @Test
    @DisplayName("Execute should not change status if not PENDING")
    void testExecuteNonPending() {
        task.setStatus(Status.RUNNING);
        double initialPower = task.getPowerConsumption();
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());
        assertEquals(initialPower, task.getPowerConsumption());
    }

    @ParameterizedTest
    @ValueSource(ints = {50, 90, 140, 150})
    @DisplayName("Priority should be set within valid range")
    void testInvalidPriorityBounds(int invalidPriority) {
        task.setPriority(invalidPriority);
        int actualPriority = task.getPriority();
        assertTrue(actualPriority >= 100 && actualPriority <= 139,
                "Priority should be within bounds 100-139, but was: " + actualPriority);
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 110, 120, 130, 139})
    @DisplayName("Valid priority values should be accepted")
    void testValidPriority(int validPriority) {
        task.setPriority(validPriority);
        assertEquals(validPriority, task.getPriority());
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1})
    @DisplayName("Memory size should not be negative")
    void testNegativeMemorySize(int negativeSize) {
        task.setMemorySize(negativeSize);
        assertEquals(0, task.getMemorySize());
    }

    @ParameterizedTest
    @ValueSource(ints = {1024, 2048, 4096})
    @DisplayName("Valid memory sizes should be accepted")
    void testValidMemorySize(int validSize) {
        task.setMemorySize(validSize);
        assertEquals(validSize, task.getMemorySize());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -50.0, -1.0})
    @DisplayName("Power consumption should not be negative")
    void testNegativePowerConsumption(double negativeValue) {
        task.setPowerConsumption(negativeValue);
        assertEquals(0.0, task.getPowerConsumption());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 50.0, 100.0})
    @DisplayName("Valid power consumption values should be accepted")
    void testValidPowerConsumption(double validValue) {
        task.setPowerConsumption(validValue);
        assertEquals(validValue, task.getPowerConsumption());
    }

    @Test
    @DisplayName("Status should be set correctly")
    void testSetStatus() {
        for (Status newStatus : Status.values()) {
            task.setStatus(newStatus);
            assertEquals(newStatus, task.getStatus());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "110, 2048",
            "120, 4096",
            "130, 1024"
    })
    @DisplayName("Execution time should be recalculated correctly")
    void testExecutionTimeCalculation(int priority, int memorySize) {
        task.setPriority(priority);
        task.setMemorySize(memorySize);

        long expectedBaseTime = 100;
        double expectedPriorityFactor = (priority - 100) / 39.0;
        double expectedMemoryFactor = memorySize / 1024.0;
        long expectedTime = (long)(expectedBaseTime * (1 + expectedPriorityFactor) * (1 + expectedMemoryFactor));

        assertEquals(expectedTime, task.getExecutionTime());
    }

    @Test
    @DisplayName("Power consumption should be calculated correctly")
    void testPowerConsumptionCalculation() {
        task.execute();
        double expectedPower = INITIAL_MEMORY_SIZE * 0.01 * INITIAL_PRIORITY;
        assertEquals(expectedPower, task.getPowerConsumption(), 0.001);
    }
}