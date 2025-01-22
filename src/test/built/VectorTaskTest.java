package test.built;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import status.Operation;
import status.Status;
import task.VectorTask;

import static org.junit.jupiter.api.Assertions.*;

public class VectorTaskTest {
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
                INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, INITIAL_VECTOR_SIZE, INITIAL_OPERATION);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructor() {
        assertNotNull(task);
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_COMPUTE_UNITS, task.getComputeUnits());
        assertEquals(INITIAL_BATCH_SIZE, task.getBatchSize());
        assertEquals(INITIAL_VECTOR_SIZE, task.getVectorSize());
        assertEquals(INITIAL_OPERATION, task.getVectorOperation());
        assertEquals(Status.PENDING, task.getStatus());
        assertTrue(task.getExecutionTime() > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Vector size should not be less than 1")
    void testInvalidVectorSize(int invalidSize) {
        task.setVectorSize(invalidSize);
        assertEquals(1, task.getVectorSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 512, 1024, 2048, 4096})
    @DisplayName("Valid vector sizes should be accepted")
    void testValidVectorSize(int validSize) {
        task.setVectorSize(validSize);
        assertEquals(validSize, task.getVectorSize());
    }

    @Test
    @DisplayName("Operation effects should be consistent")
    void testOperationEffects() {
        task.setVectorOperation(Operation.ADD);
        long baseTime = task.getExecutionTime();

        task.setVectorOperation(Operation.MUL);
        assertEquals(1.2 * baseTime, task.getExecutionTime(), baseTime * 0.1);

        task.setVectorOperation(Operation.REDUCE);
        assertEquals(1.5 * baseTime, task.getExecutionTime(), baseTime * 0.1);
    }

    @Test
    @DisplayName("Optimization should reduce execution time by 30%")
    void testOptimization() {
        long initialTime = task.getExecutionTime();
        assertFalse(task.isOptimized());

        task.optimize();
        assertTrue(task.isOptimized());
        assertEquals(0.7 * initialTime, task.getExecutionTime(), initialTime * 0.1);

        long optimizedTime = task.getExecutionTime();
        task.optimize();
        assertEquals(optimizedTime, task.getExecutionTime());
    }

    @Test
    @DisplayName("Null operation should use base execution time")
    void testNullOperation() {
        long baseTime = task.getExecutionTime();
        Operation previousOp = task.getVectorOperation();

        task.setVectorOperation(null);
        long nullOpTime = task.getExecutionTime();
        assertTrue(nullOpTime < baseTime,
                String.format("Null operation time (%d) should be less than base time (%d)",
                        nullOpTime, baseTime));
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    @DisplayName("All operations should affect execution time correctly")
    void testAllOperations(Operation operation) {
        task.setVectorOperation(operation);
        long executionTime = task.getExecutionTime();
        assertTrue(executionTime > 0);
    }

    @Test
    @DisplayName("Vector size scaling should follow logarithmic progression")
    void testVectorSizeScaling() {
        task.setVectorSize(1024);
        long baseTime = task.getExecutionTime();

        task.setVectorSize(2048);
        long doubledTime = task.getExecutionTime();

        // The ratio should be approximately log2(2048) / log2(1024)
        double expectedRatio = Math.log10(2048) / Math.log10(1024);
        double actualRatio = (double) doubledTime / baseTime;
        assertEquals(expectedRatio, actualRatio, 0.01);
    }

    @Test
    @DisplayName("Combined effects should work correctly")
    void testCombinedEffects() {
        long initialTime = task.getExecutionTime();

        task.setVectorSize(2048);          // Increase size
        task.setVectorOperation(Operation.MUL);  // Change operation
        task.optimize();                   // Optimize

        long finalTime = task.getExecutionTime();
        assertTrue(finalTime > 0);
        assertNotEquals(initialTime, finalTime);
    }

    @Test
    @DisplayName("Execution time calculation should handle edge cases")
    void testEdgeCases() {
        VectorTask minTask = new VectorTask(TASK_ID, 100, 0,
                1, 2, 1, Operation.ADD);
        assertTrue(minTask.getExecutionTime() > 0);

        VectorTask maxTask = new VectorTask(TASK_ID, 139, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Operation.REDUCE);
        assertTrue(maxTask.getExecutionTime() > 0);
        assertTrue(maxTask.getExecutionTime() < Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Task should maintain inherited behavior")
    void testInheritance() {
        assertEquals(Status.PENDING, task.getStatus());
        task.setStatus(Status.RUNNING);
        assertEquals(Status.RUNNING, task.getStatus());

        task.setPriority(130);
        assertEquals(130, task.getPriority());

        task.setComputeUnits(8);
        assertEquals(8, task.getComputeUnits());

        task.setBatchSize(32);
        assertEquals(32, task.getBatchSize());
    }
}