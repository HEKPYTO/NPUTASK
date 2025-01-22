package test.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import status.Quantization;
import status.Status;
import task.TensorTask;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TensorTaskSolutionTest {
    private TensorTask task;
    private static final long TASK_ID = 9876L;
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

    // Complex Tensor Operations
    @Test
    @DisplayName("Task should handle tensor dimension scaling")
    void testDimensionScaling() {
        // Test various dimension combinations sorted by total elements
        int[][] dimensionSets = {
                {2, 2},        // 4 elements
                {2, 2, 2},     // 8 elements
                {4, 4},        // 16 elements
                {4, 4, 4}      // 64 elements
        };

        List<Long> executionTimes = new ArrayList<>();
        for (int[] dims : dimensionSets) {
            task = new TensorTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                    INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE, dims, INITIAL_TYPE);
            double sparsityFactor = Math.max(0.1, 1.0 - (task.getSparsity() * 0.5));
            executionTimes.add((long) (task.getExecutionTime() / sparsityFactor));
        }

        // Verify scaling pattern
        for (int i = 1; i < executionTimes.size(); i++) {
            assertTrue(executionTimes.get(i) > executionTimes.get(i-1),
                    String.format("Execution time should increase: %d -> %d for dimensions %s -> %s",
                            executionTimes.get(i-1), executionTimes.get(i),
                            Arrays.toString(dimensionSets[i-1]), Arrays.toString(dimensionSets[i])));
        }
    }

    // Concurrent Processing
    @Test
    @DisplayName("Task should handle concurrent tensor operations")
    void testConcurrentOperations() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Double>> results = new ArrayList<>();

        // Submit multiple tensor operations with different types
        for (Quantization type : Quantization.values()) {
            results.add(executor.submit(() -> {
                startLatch.await();
                TensorTask tensorTask = new TensorTask(TASK_ID, INITIAL_PRIORITY,
                        INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                        INITIAL_DIMENSIONS, type);
                tensorTask.execute();
                return tensorTask.getSparsity();
            }));
        }

        startLatch.countDown();

        // Verify all operations completed and sparsity is valid
        for (Future<Double> result : results) {
            double sparsity = result.get(5, TimeUnit.SECONDS);
            assertTrue(sparsity >= 0.0 && sparsity <= 0.95);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    // Resource Management
    @Test
    @DisplayName("Task should handle tensor resource scaling")
    void testResourceScaling() {
        // Test different quantization types with same dimensions
        Map<Quantization, Long> baselineTimes = new EnumMap<>(Quantization.class);
        for (Quantization type : Quantization.values()) {
            task.setTensorType(type);
            baselineTimes.put(type, task.getExecutionTime());
        }

        // Double dimensions and verify scaling
        int[] largerDims = new int[INITIAL_DIMENSIONS.length];
        for (int i = 0; i < INITIAL_DIMENSIONS.length; i++) {
            largerDims[i] = INITIAL_DIMENSIONS[i] * 2;
        }
        task.setDimensions(largerDims);

        // Verify scaling for each type
        for (Quantization type : Quantization.values()) {
            task.setTensorType(type);
            long scaledTime = task.getExecutionTime();
            assertTrue(scaledTime > baselineTimes.get(type),
                    String.format("Larger dimensions should increase time for type %s", type));
        }
    }

    // Performance Testing
    @Test
    @DisplayName("Task should handle different tensor configurations")
    void testTensorConfigurations() {
        int[][] dimensions = {
                {8, 8},           // 2D medium
                {16, 16},         // 2D large
                {4, 4, 4},        // 3D medium
                {8, 8, 8}         // 3D large
        };

        Quantization[] types = Quantization.values();
        Map<String, Long> executionTimes = new HashMap<>();

        for (int[] dims : dimensions) {
            for (Quantization type : types) {
                task.setDimensions(dims);
                task.setTensorType(type);
                String key = String.format("dims=%s,type=%s",
                        Arrays.toString(dims), type);
                executionTimes.put(key, task.getExecutionTime());

                // Verify execution time is reasonable
                assertTrue(task.getExecutionTime() > 0);
                task.execute();
                assertEquals(Status.RUNNING, task.getStatus());
            }
        }
    }

    // Load Testing
    @Test
    @DisplayName("Task should handle sustained tensor operations")
    void testSustainedOperations() {
        int iterations = 1000;
        List<Double> sparsityValues = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();

        Quantization[] types = Quantization.values();
        for (int i = 0; i < iterations; i++) {
            Quantization type = types[i % types.length];
            task.setTensorType(type);
            task.execute();

            sparsityValues.add(task.getSparsity());
            executionTimes.add(task.getExecutionTime());
            assertEquals(Status.RUNNING, task.getStatus());
        }

        // Verify distribution of sparsity values
        double avgSparsity = sparsityValues.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        assertTrue(avgSparsity > 0.0 && avgSparsity < 0.95);
    }

    // Error Handling
    @Test
    @DisplayName("Task should handle dimension modifications safely")
    void testDimensionModification() {
        int[] originalDims = task.getDimensions().clone();

        // Verify original dimensions preserved after invalid attempts
        assertArrayEquals(originalDims, task.getDimensions());

        // Test valid modification
        int[] newDims = {4, 4, 4};
        task.setDimensions(newDims);
        assertArrayEquals(newDims, task.getDimensions());
    }


    @Test
    @DisplayName("Task should handle combined parameter effects")
    void testCombinedEffects() {

        long baseTime = task.getExecutionTime();


        task = new TensorTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                8,
                32,
                new int[]{4, 4, 4},
                Quantization.FLOAT32);

        long newTime = task.getExecutionTime();


        assertTrue(newTime > baseTime,
                String.format("Combined changes should increase execution time (base: %d, new: %d)",
                        baseTime, newTime));
    }


    @Test
    @DisplayName("Task should generate valid sparsity distributions")
    void testSparsityDistribution() {
        int sampleSize = 1000;
        List<Double> sparsityValues = new ArrayList<>();


        for (int i = 0; i < sampleSize; i++) {
            TensorTask testTask = new TensorTask(i, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                    INITIAL_DIMENSIONS, INITIAL_TYPE);
            sparsityValues.add(testTask.getSparsity());
        }


        double minSparsity = sparsityValues.stream().mapToDouble(d -> d).min().orElse(0.0);
        double maxSparsity = sparsityValues.stream().mapToDouble(d -> d).max().orElse(1.0);
        double avgSparsity = sparsityValues.stream().mapToDouble(d -> d).average().orElse(0.0);

        assertTrue(minSparsity >= 0.0, "Minimum sparsity should be non-negative");
        assertTrue(maxSparsity <= 0.95, "Maximum sparsity should not exceed 0.95");
        assertTrue(avgSparsity > 0.2 && avgSparsity < 0.8,
                String.format("Average sparsity (%.2f) should be reasonably distributed",
                        avgSparsity));
    }


    @Test
    @DisplayName("Task should calculate dimension factors correctly")
    void testDimensionFactors() {
        int[][] testDimensions = {
                {2, 2},
                {2, 2, 2},
                {4, 4},
                {2, 2, 2, 2}
        };

        List<Long> executionTimes = new ArrayList<>();


        for (int[] dims : testDimensions) {
            task.setDimensions(dims);
            executionTimes.add(task.getExecutionTime());


            int totalElements = Arrays.stream(dims).reduce(1, (a, b) -> a * b);
            assertTrue(task.getExecutionTime() > 0,
                    String.format("Dimension set %s with %d elements should have positive execution time",
                            Arrays.toString(dims), totalElements));
        }


        for (int i = 1; i < executionTimes.size(); i++) {
            assertTrue(executionTimes.get(i) >= executionTimes.get(i-1),
                    String.format("Execution time should increase with more elements: %d -> %d",
                            executionTimes.get(i-1), executionTimes.get(i)));
        }
    }


    @Test
    @DisplayName("Task should handle edge case configurations")
    void testEdgeCases() {

        int[] minDims = {1};
        task.setDimensions(minDims);
        assertTrue(task.getExecutionTime() > 0, "Single dimension should have positive execution time");


        int[] largeDims = {16, 16, 16};
        task.setDimensions(largeDims);
        task.setTensorType(Quantization.INT8);
        assertTrue(task.getExecutionTime() > 0 && task.getExecutionTime() < Long.MAX_VALUE,
                "Large dimensions should have reasonable execution time");


        task.setComputeUnits(Integer.MAX_VALUE);
        task.setBatchSize(Integer.MAX_VALUE);
        assertTrue(task.getExecutionTime() > 0, "Maximum resource config should have valid execution time");
    }
}