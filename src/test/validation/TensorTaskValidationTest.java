package test.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import status.Quantization;
import status.Status;
import task.TensorTask;

import static org.junit.jupiter.api.Assertions.*;

public class TensorTaskValidationTest {
    private TensorTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final int INITIAL_COMPUTE_UNITS = 4;
    private static final int INITIAL_BATCH_SIZE = 16;
    private static final int[] INITIAL_DIMENSIONS = {2, 3, 4};
    private static final Quantization INITIAL_TYPE = Quantization.BFLOAT16;

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
    @DisplayName("Dimensions should be immutable")
    void testDimensionImmutability() {
        int[] originalDims = task.getDimensions();
        int[] returnedDims = task.getDimensions();

        returnedDims[0] = 999;
        assertNotEquals(returnedDims[0], task.getDimensions()[0]);

        assertArrayEquals(originalDims, task.getDimensions());
    }

    @ParameterizedTest
    @EnumSource(Quantization.class)
    @DisplayName("All tensor types should be accepted")
    void testValidTensorTypes(Quantization type) {
        task.setTensorType(type);
        assertEquals(type, task.getTensorType());
    }

    @Test
    @DisplayName("Null tensor type should be handled")
    void testNullTensorType() {
        task.setTensorType(null);
        assertNull(task.getTensorType());
        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Sparsity should be within valid range")
    void testSparsityRange() {
        double sparsity = task.getSparsity();
        assertTrue(sparsity >= 0.0 && sparsity <= 0.95,
                String.format("Sparsity %f should be between 0.0 and 0.95", sparsity));
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