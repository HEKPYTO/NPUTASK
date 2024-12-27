package test.built.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import status.Quantization;
import task.compute.advanced.TensorTask;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TensorTaskTest {
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
                INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, INITIAL_DIMENSIONS, INITIAL_TYPE);
    }

    @Test
    @DisplayName("Constructor should handle various valid inputs")
    void testConstructorValidCases() {
        TensorTask minTask = new TensorTask(Long.MIN_VALUE, 100, 0, 1, 2, new int[]{1}, Quantization.INT8);
        assertNotNull(minTask);
        assertEquals(Long.MIN_VALUE, minTask.getTaskId());
        assertEquals(100, minTask.getPriority());
        assertArrayEquals(new int[]{1}, minTask.getDimensions());
        assertEquals(Quantization.INT8, minTask.getTensorType());
        assertTrue(minTask.getExecutionTime() > 0);

        TensorTask maxTask = new TensorTask(Long.MAX_VALUE, 139, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, new int[]{100, 100}, Quantization.FLOAT32);
        assertNotNull(maxTask);
        assertTrue(maxTask.getExecutionTime() > 0);
        assertTrue(maxTask.getExecutionTime() < Long.MAX_VALUE);

        int[][] dimensionCases = {
                {1, 1, 1},           // Minimum dimensions
                {10, 10, 10},        // Medium dimensions
                {2, 3, 4, 5},        // More dimensions
                {100}                // Single dimension
        };

        for (int[] dims : dimensionCases) {
            TensorTask dimTask = new TensorTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                    INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, dims, INITIAL_TYPE);
            assertArrayEquals(dims, dimTask.getDimensions());
            assertTrue(dimTask.getExecutionTime() > 0);
        }
    }

    @Test
    @DisplayName("Dimension setter should handle various cases")
    void testSetDimensions() {
        int[][] validDims = {
                {1},                 // Single dimension
                {2, 2},             // 2D
                {2, 2, 2},          // 3D
                {1, 2, 3, 4, 5},    // Many dimensions
                {100, 100}          // Large dimensions
        };

        for (int[] dims : validDims) {
            task.setDimensions(dims);
            assertArrayEquals(dims, task.getDimensions());
            assertTrue(task.getExecutionTime() > 0);
        }
    }

    @Test
    @DisplayName("Tensor type setter should handle all cases")
    void testSetTensorType() {
        for (Quantization type : Quantization.values()) {
            task.setTensorType(type);
            assertEquals(type, task.getTensorType());
            assertTrue(task.getExecutionTime() > 0);
        }

        long prevTime = task.getExecutionTime();
        task.setTensorType(null);
        assertNull(task.getTensorType());
        assertNotEquals(prevTime, task.getExecutionTime());
    }

    @Test
    @DisplayName("Execution time calculation should be consistent")
    void testExecutionTimeCalculation() {
        long baseTime = task.getExecutionTime();
        assertTrue(baseTime > 0, "Base execution time should be positive");

        task.setTensorType(Quantization.FLOAT32);
        long float32Time = task.getExecutionTime();
        double expectedFloat32Ratio = 1.8;
        assertEquals(baseTime * expectedFloat32Ratio, float32Time, baseTime * 0.1,
                "FLOAT32 should increase time by factor of 1.8");

        task.setTensorType(Quantization.INT8);
        long int8Time = task.getExecutionTime();
        double expectedInt8Ratio = 0.4;
        assertEquals(baseTime * expectedInt8Ratio, int8Time, baseTime * 0.1,
                "INT8 should decrease time by factor of 0.4");

        task.setTensorType(INITIAL_TYPE);
        task.setDimensions(new int[]{4, 4, 4});
        long newDimTime = task.getExecutionTime();
        assertTrue(newDimTime > baseTime,
                String.format("Larger dimensions should increase time: %d > %d", newDimTime, baseTime));

        task.setTensorType(Quantization.FLOAT32);
        long combinedTime = task.getExecutionTime();
        assertTrue(combinedTime > baseTime,
                String.format("Combined effects should increase time: %d > %d", combinedTime, baseTime));
        assertTrue(combinedTime > newDimTime,
                String.format("FLOAT32 should further increase time: %d > %d", combinedTime, newDimTime));

        assertTrue(combinedTime < Long.MAX_VALUE / 2,
                "Execution time should not approach Long.MAX_VALUE");
    }

    @Test
    @DisplayName("Dimension scaling should follow expected ratios")
    void testDimensionScaling() {
        task.setDimensions(new int[]{2, 2});
        long baseTime = task.getExecutionTime();

        task.setDimensions(new int[]{4, 4});
        long scaledTime = task.getExecutionTime();

        double expectedRatio = Math.log10(16) / Math.log10(4);
        double actualRatio = (double) scaledTime / baseTime;

        assertEquals(expectedRatio, actualRatio, 0.2,
                String.format("Expected ratio %.2f but got %.2f", expectedRatio, actualRatio));
    }

    @ParameterizedTest
    @EnumSource(Quantization.class)
    @DisplayName("Tensor type changes should have consistent effects")
    void testTensorTypeEffects(Quantization type) {
        task.setTensorType(INITIAL_TYPE);
        long baseTime = task.getExecutionTime();

        task.setTensorType(type);
        long newTime = task.getExecutionTime();

        double expectedFactor = switch(type) {
            case FLOAT32 -> 1.8;
            case INT8 -> 0.4;
            case BFLOAT16 -> 1.0;
        };

        double actualFactor = (double) newTime / baseTime;
        assertEquals(expectedFactor, actualFactor, 0.1,
                String.format("%s type factor: expected %.2f but got %.2f",
                        type, expectedFactor, actualFactor));
    }

    @Test
    @DisplayName("Sparsity calculation should be within bounds")
    void testSparsityCalculation() {
        for (int i = 0; i < 100; i++) {
            TensorTask newTask = new TensorTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                    INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, INITIAL_DIMENSIONS, INITIAL_TYPE);
            double sparsity = newTask.getSparsity();
            assertTrue(sparsity >= 0.0 && sparsity <= 0.95,
                    "Sparsity " + sparsity + " should be between 0.0 and 0.95");
        }
    }

    @Test
    @DisplayName("Dimension immutability should be guaranteed")
    void testDimensionImmutability() {
        int[] originalDims = task.getDimensions();
        int[] returnedDims = task.getDimensions();

        returnedDims[0] = 999;
        assertNotEquals(returnedDims[0], task.getDimensions()[0]);

        assertArrayEquals(originalDims, task.getDimensions());

        int[] newDims = {5, 5, 5};
        task.setDimensions(newDims);
        newDims[0] = 999;
        assertNotEquals(newDims[0], task.getDimensions()[0]);
    }

    @Test
    @DisplayName("Task should handle extreme cases")
    void testExtremeCases() {
        task.setDimensions(new int[]{Integer.MAX_VALUE});
        assertTrue(task.getExecutionTime() > 0);
        assertTrue(task.getExecutionTime() < Long.MAX_VALUE);

        int[] manyDims = new int[100];
        Arrays.fill(manyDims, 2);
        task.setDimensions(manyDims);
        assertTrue(task.getExecutionTime() > 0);

        task.setTensorType(Quantization.FLOAT32);
        assertTrue(task.getExecutionTime() < Long.MAX_VALUE);
    }
}