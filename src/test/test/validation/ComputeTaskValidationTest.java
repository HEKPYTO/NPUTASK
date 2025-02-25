package test.test.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import status.Status;
import task.ComputeTask;

import static org.junit.jupiter.api.Assertions.*;

public class ComputeTaskValidationTest {
    private ComputeTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final int INITIAL_COMPUTE_UNITS = 4;
    private static final int INITIAL_BATCH_SIZE = 16;

    @BeforeEach
    void setUp() {
        task = new ComputeTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructorInitialization() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_COMPUTE_UNITS, task.getComputeUnits());
        assertEquals(INITIAL_BATCH_SIZE, task.getBatchSize());
        assertEquals(Status.PENDING, task.getStatus());
        assertTrue(task.getExecutionTime() > 0);
        assertTrue(task.getEfficiency() > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
    @DisplayName("Compute units should not be less than 1")
    void testInvalidComputeUnits(int invalidUnits) {
        task.setComputeUnits(invalidUnits);
        assertEquals(1, task.getComputeUnits(),
                "Compute units should be minimum 1");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, Integer.MAX_VALUE})
    @DisplayName("Valid compute units should be accepted")
    void testValidComputeUnits(int validUnits) {
        task.setComputeUnits(validUnits);
        assertEquals(validUnits, task.getComputeUnits());
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -1, 0, 1})
    @DisplayName("Batch size should not be less than 2 and should be even")
    void testInvalidBatchSize(int invalidSize) {
        task.setBatchSize(invalidSize);
        assertTrue(task.getBatchSize() >= 2,
                "Batch size should be minimum 2");
        assertEquals(0, task.getBatchSize() % 2,
                "Batch size should be even");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8, 16, 32})
    @DisplayName("Valid batch sizes should be accepted")
    void testValidBatchSize(int validSize) {
        task.setBatchSize(validSize);
        assertEquals(validSize, task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 7, 9, 11})
    @DisplayName("Odd batch sizes should be rounded down to even")
    void testOddBatchSize(int oddSize) {
        task.setBatchSize(oddSize);
        assertEquals((oddSize / 2) * 2, task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 16, 0.64",   // Base case
            "8, 32, 2.56",   // Higher values
            "2, 8, 0.16",    // Lower values
            "1, 2, 0.02"     // Minimum values
    })
    @DisplayName("Efficiency should be calculated correctly")
    void testEfficiencyCalculation(int computeUnits, int batchSize, double expectedEfficiency) {
        task.setComputeUnits(computeUnits);
        task.setBatchSize(batchSize);
        assertEquals(expectedEfficiency, task.getEfficiency(), 0.001);
    }

    @Test
    @DisplayName("Execution time should consider compute and batch factors")
    void testExecutionTimeCalculation() {
        ComputeTask baseTask = new ComputeTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, 1, 16);
        long baseTime = baseTask.getExecutionTime();

        task.setComputeUnits(4);
        task.setBatchSize(32);

        double expectedFactor = (1.0 / 4) * (32.0 / 16.0);
        long expectedTime = (long)(baseTime * expectedFactor);
        assertEquals(expectedTime, task.getExecutionTime());
    }

    @Test
    @DisplayName("Task should maintain inherited NPUTask behavior")
    void testInheritedBehavior() {
        assertEquals(Status.PENDING, task.getStatus());
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.setPriority(150);
        assertEquals(139, task.getPriority());
        task.setPriority(90);
        assertEquals(100, task.getPriority());

        task.setMemorySize(-1024);
        assertEquals(0, task.getMemorySize());
    }
}