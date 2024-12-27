package test.built.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import status.Status;
import task.compute.ComputationTask;

import static org.junit.jupiter.api.Assertions.*;

class ComputationTaskTest {
    private ComputationTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final int INITIAL_COMPUTE_UNITS = 4;
    private static final int INITIAL_BATCH_SIZE = 16;

    @BeforeEach
    void setUp() {
        task = new ComputationTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructor() {
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
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Compute units should not be less than 1")
    void testInvalidComputeUnits(int invalidUnits) {
        task.setComputeUnits(invalidUnits);
        assertEquals(1, task.getComputeUnits(),
                "Compute units should be minimum 1, but was: " + task.getComputeUnits());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16})
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
                "Batch size should be minimum 2, but was: " + task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2,
                "Batch size should be even, but was: " + task.getBatchSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8, 16, 32})
    @DisplayName("Valid batch sizes should be accepted and rounded to even numbers")
    void testValidBatchSize(int validSize) {
        task.setBatchSize(validSize);
        assertEquals(validSize, task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 7, 9})
    @DisplayName("Odd batch sizes should be rounded down to even numbers")
    void testOddBatchSize(int oddSize) {
        task.setBatchSize(oddSize);
        assertEquals((oddSize / 2) * 2, task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 16, 0.64",
            "8, 32, 2.56",
            "2, 8, 0.16",
            "1, 2, 0.02"
    })
    @DisplayName("Efficiency should be calculated correctly")
    void testEfficiencyCalculation(int computeUnits, int batchSize, double expectedEfficiency) {
        task.setComputeUnits(computeUnits);
        task.setBatchSize(batchSize);
        assertEquals(expectedEfficiency, task.getEfficiency(), 0.001);
    }

    @Test
    @DisplayName("Execution time should be calculated correctly with compute and batch factors")
    void testExecutionTimeCalculation() {
        // First get the base execution time by creating a task with compute units = 1 and batch size = 16
        ComputationTask baseTask = new ComputationTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE, 1, 16);
        long baseExecutionTime = baseTask.getExecutionTime();

        // different combinations
        task.setComputeUnits(4);
        task.setBatchSize(32);

        // Expected time = base * (1 / computeUnits) * (batchSize / 16)
        double expectedFactor = (1.0 / 4) * (32.0 / 16.0);
        long expectedTime = (long)(baseExecutionTime * expectedFactor);

        assertEquals(expectedTime, task.getExecutionTime());
    }

    @Test
    @DisplayName("Changing compute units should update both execution time and efficiency")
    void testComputeUnitsUpdatesDependentValues() {
        long initialExecutionTime = task.getExecutionTime();
        double initialEfficiency = task.getEfficiency();

        task.setComputeUnits(8);

        assertNotEquals(initialExecutionTime, task.getExecutionTime());
        assertNotEquals(initialEfficiency, task.getEfficiency());
    }

    @Test
    @DisplayName("Changing batch size should update both execution time and efficiency")
    void testBatchSizeUpdatesDependentValues() {
        long initialExecutionTime = task.getExecutionTime();
        double initialEfficiency = task.getEfficiency();

        task.setBatchSize(32);

        assertNotEquals(initialExecutionTime, task.getExecutionTime());
        assertNotEquals(initialEfficiency, task.getEfficiency());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2",    // Minimum values
            "16, 64",  // Large values
            "4, 16",   // Medium values
            "8, 32"    // Other valid combination
    })
    @DisplayName("Constructor should handle different valid combinations")
    void testConstructorWithDifferentCombinations(int computeUnits, int batchSize) {
        ComputationTask newTask = new ComputationTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, computeUnits, batchSize);

        assertEquals(computeUnits, newTask.getComputeUnits());
        assertEquals(batchSize, newTask.getBatchSize());
        assertTrue(newTask.getExecutionTime() > 0);
        assertTrue(newTask.getEfficiency() > 0);
    }

    @Test
    @DisplayName("Inherited properties should be maintained")
    void testInheritedProperties() {
        task.setStatus(Status.RUNNING);
        assertEquals(Status.RUNNING, task.getStatus());

        task.setPriority(130);
        assertEquals(130, task.getPriority());

        task.setMemorySize(4096);
        assertEquals(4096, task.getMemorySize());
    }
}