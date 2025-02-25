package test.test.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Quantization;
import status.Status;
import task.TensorTask;

import static org.junit.jupiter.api.Assertions.*;

public class TensorTaskSolutionTest {
    private TensorTask task;
    private static final long TASK_ID = 9876L;
    private static final int INITIAL_PRIORITY = 125;
    private static final int INITIAL_MEMORY_SIZE = 4096;
    private static final int INITIAL_COMPUTE_UNITS = 8;
    private static final int INITIAL_BATCH_SIZE = 32;
    private static final int[] INITIAL_DIMENSIONS = {3, 4, 5};
    private static final Quantization INITIAL_TYPE = Quantization.INT8;

    @BeforeEach
    void setUp() {
        task = new TensorTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, INITIAL_DIMENSIONS,
                INITIAL_TYPE);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructorInitialization() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_COMPUTE_UNITS, task.getComputeUnits());
        assertEquals(INITIAL_BATCH_SIZE, task.getBatchSize());
        assertArrayEquals(INITIAL_DIMENSIONS, task.getDimensions());
        assertEquals(INITIAL_TYPE, task.getTensorType());
        assertTrue(task.getSparsity() >= 0.0 && task.getSparsity() <= 0.95);
        assertEquals(Status.PENDING, task.getStatus());
    }

    @Test
    @DisplayName("Null tensor type should be handled")
    void testNullTensorType() {
        task.setTensorType(null);
        assertNull(task.getTensorType());
        assertTrue(task.getExecutionTime() > 0);
    }

    @ParameterizedTest
    @CsvSource({
            "FLOAT32, 1.8",
            "INT8, 0.4",
            "BFLOAT16, 1.0"
    })
    @DisplayName("Tensor type should affect execution time correctly")
    void testTensorTypeFactors(Quantization type, double expectedFactor) {
        TensorTask referenceTask = new TensorTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                INITIAL_DIMENSIONS, Quantization.BFLOAT16);
        long referenceTime = referenceTask.getExecutionTime();
        double refSparsity = Math.max(0.1, 1.0 - (referenceTask.getSparsity() * 0.5));

        TensorTask testTask = new TensorTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                INITIAL_DIMENSIONS, type);
        long typeTime = testTask.getExecutionTime();
        double typeSparsity = Math.max(0.1, 1.0 - (testTask.getSparsity() * 0.5));

        assertEquals(referenceTime * expectedFactor / refSparsity, typeTime / typeSparsity, referenceTime * 0.01,
                String.format("Type %s should multiply execution time by %.1f",
                        type, expectedFactor));
    }

    @Test
    @DisplayName("Task should maintain inherited ComputeTask behavior")
    void testInheritedBehavior() {
        task.setComputeUnits(0);
        assertEquals(1, task.getComputeUnits());

        task.setBatchSize(5);
        assertEquals(4, task.getBatchSize());
        assertEquals(0, task.getBatchSize() % 2);

        assertEquals(Status.PENDING, task.getStatus());
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());
    }
}