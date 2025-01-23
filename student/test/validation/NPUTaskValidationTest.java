package test.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import status.Status;
import task.NPUTask;

import static org.junit.jupiter.api.Assertions.*;

public class NPUTaskValidationTest {
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
    void testConstructorInitialization() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(Status.PENDING, task.getStatus());
        assertEquals(0.0, task.getPowerConsumption());
        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Constructor should handle boundary values")
    void testConstructorBoundaries() {
        NPUTask minTask = new NPUTask(Long.MIN_VALUE, 100, 0);
        assertEquals(Long.MIN_VALUE, minTask.getTaskId());
        assertEquals(100, minTask.getPriority());
        assertEquals(0, minTask.getMemorySize());

        NPUTask maxTask = new NPUTask(Long.MAX_VALUE, 139, Integer.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, maxTask.getTaskId());
        assertEquals(139, maxTask.getPriority());
        assertTrue(maxTask.getMemorySize() > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {90, 95, 140, 145, Integer.MIN_VALUE, Integer.MAX_VALUE})
    @DisplayName("Priority should be clamped to valid range [100-139]")
    void testPriorityBoundaries(int invalidPriority) {
        task.setPriority(invalidPriority);
        int actualPriority = task.getPriority();
        assertTrue(actualPriority >= 100 && actualPriority <= 139,
                "Priority should be within [100, 139], but was: " + actualPriority);
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 110, 120, 130, 139})
    @DisplayName("Valid priorities should be accepted")
    void testValidPriorities(int validPriority) {
        task.setPriority(validPriority);
        assertEquals(validPriority, task.getPriority());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1024, -512, -1, Integer.MIN_VALUE})
    @DisplayName("Memory size should not be negative")
    void testNegativeMemorySize(int negativeSize) {
        task.setMemorySize(negativeSize);
        assertEquals(0, task.getMemorySize());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1024, 2048, 4096, Integer.MAX_VALUE})
    @DisplayName("Valid memory sizes should be accepted")
    void testValidMemorySize(int validSize) {
        task.setMemorySize(validSize);
        assertEquals(validSize, task.getMemorySize());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 1024",    // Base case
            "120, 2048",    // Medium case
            "139, 4096"     // Max case
    })
    @DisplayName("Execution time should be calculated correctly")
    void testExecutionTimeCalculation(int priority, int memorySize) {
        task.setPriority(priority);
        task.setMemorySize(memorySize);

        long expectedBaseTime = 100;
        double expectedPriorityFactor = (priority - 100) / 39.0;
        double expectedMemoryFactor = memorySize / 1024.0;
        long expectedTime = (long)(expectedBaseTime *
                (1 + expectedPriorityFactor) * (1 + expectedMemoryFactor));

        assertEquals(expectedTime, task.getExecutionTime());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 1024, 1024.0",  // Minimum priority
            "120, 2048, 2457.6",  // Medium case
            "139, 4096, 5693.44"  // Maximum case
    })
    @DisplayName("Power consumption should be calculated correctly")
    void testPowerConsumptionCalculation(int priority, int memorySize, double expectedPower) {
        NPUTask powerTask = new NPUTask(1L, priority, memorySize);
        powerTask.execute();
        assertEquals(expectedPower, powerTask.getPowerConsumption(), 0.01);
    }

    @Test
    @DisplayName("Status transitions should be valid")
    void testStatusTransitions() {
        for (Status newStatus : Status.values()) {
            task.setStatus(newStatus);
            assertEquals(newStatus, task.getStatus());
        }
    }

    @Test
    @DisplayName("Execute should only change status if PENDING")
    void testExecuteStatusChange() {
        assertEquals(Status.PENDING, task.getStatus());
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.setStatus(Status.COMPLETED);
        task.execute();
        assertEquals(Status.COMPLETED, task.getStatus());

        task.setStatus(Status.FAILED);
        task.execute();
        assertEquals(Status.FAILED, task.getStatus());
    }
}

