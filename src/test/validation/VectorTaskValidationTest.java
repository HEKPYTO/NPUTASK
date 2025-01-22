package test.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import status.Operation;
import status.Status;
import task.VectorTask;

import static org.junit.jupiter.api.Assertions.*;

public class VectorTaskValidationTest {
    private VectorTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final int INITIAL_COMPUTE_UNITS = 4;
    private static final int INITIAL_BATCH_SIZE = 16;
    private static final int INITIAL_VECTOR_SIZE = 1024;
    private static final Operation INITIAL_OPERATION = Operation.ADD;

    @BeforeEach
    void setUp() {
        task = new VectorTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, INITIAL_VECTOR_SIZE,
                INITIAL_OPERATION);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructorInitialization() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_COMPUTE_UNITS, task.getComputeUnits());
        assertEquals(INITIAL_BATCH_SIZE, task.getBatchSize());
        assertEquals(INITIAL_VECTOR_SIZE, task.getVectorSize());
        assertEquals(INITIAL_OPERATION, task.getVectorOperation());
        assertFalse(task.isOptimized());
        assertEquals(Status.PENDING, task.getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Vector size should not be less than 1")
    void testInvalidVectorSize(int invalidSize) {
        task.setVectorSize(invalidSize);
        assertEquals(1, task.getVectorSize(),
                "Vector size should be set to minimum value of 1");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 512, 1024, 2048, 4096})
    @DisplayName("Valid vector sizes should be accepted")
    void testValidVectorSize(int validSize) {
        task.setVectorSize(validSize);
        assertEquals(validSize, task.getVectorSize());
        assertTrue(task.getExecutionTime() > 0);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    @DisplayName("All operations should be accepted and affect execution time")
    void testOperations(Operation operation) {
        task.setVectorOperation(operation);
        assertEquals(operation, task.getVectorOperation());
        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Null operation should use default execution time")
    void testNullOperation() {
        task.setVectorOperation(null);
        assertNull(task.getVectorOperation());
        assertTrue(task.getExecutionTime() > 0);
    }

    @ParameterizedTest
    @CsvSource({
            "ADD, 1.0",
            "MUL, 1.2",
            "REDUCE, 1.5"
    })
    @DisplayName("Operation type should affect execution time correctly")
    void testOperationFactors(Operation operation, double expectedFactor) {
        VectorTask referenceTask = new VectorTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                INITIAL_VECTOR_SIZE, Operation.ADD);
        long referenceTime = referenceTask.getExecutionTime();

        task.setVectorOperation(operation);
        long operationTime = task.getExecutionTime();

        assertEquals(referenceTime * expectedFactor, operationTime, referenceTime * 0.01,
                String.format("Operation %s should multiply execution time by %.1f",
                        operation, expectedFactor));
    }

    @Test
    @DisplayName("Optimization should reduce execution time by 30%")
    void testOptimization() {
        long initialTime = task.getExecutionTime();
        assertFalse(task.isOptimized());

        task.optimize();
        assertTrue(task.isOptimized());
        assertEquals(initialTime * 0.7, task.getExecutionTime(), initialTime * 0.01,
                "Optimization should reduce execution time by 30%");

        task.optimize();
        assertEquals(initialTime * 0.7, task.getExecutionTime(), initialTime * 0.01,
                "Multiple optimizations should not further reduce time");
    }

    @Test
    @DisplayName("Vector size should scale execution time logarithmically")
    void testSizeScaling() {
        task.setVectorSize(1024);
        long baseTime = task.getExecutionTime();

        task.setVectorSize(2048);
        long doubledTime = task.getExecutionTime();

        double expectedRatio = Math.log10(2048) / Math.log10(1024);
        assertEquals(expectedRatio, (double) doubledTime / baseTime, 0.01,
                "Execution time should scale logarithmically with vector size");
    }

    @Test
    @DisplayName("Task should maintain inherited ComputationTask behavior")
    void testInheritedBehavior() {
        task.setComputeUnits(0);
        assertEquals(1, task.getComputeUnits());

        task.setBatchSize(3);
        assertEquals(2, task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2);

        assertEquals(Status.PENDING, task.getStatus());
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());
    }
}