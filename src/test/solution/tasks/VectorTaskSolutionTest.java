package test.solution.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Operation;
import status.Status;
import task.compute.advanced.VectorTask;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class VectorTaskSolutionTest {
    private VectorTask task;
    private static final long TASK_ID = 9876L;
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
    @DisplayName("Task should handle different operation combinations")
    void testOperationCombinations() {

        Map<Operation, VectorTask> operationTasks = new EnumMap<>(Operation.class);
        for (Operation op : Operation.values()) {
            operationTasks.put(op, new VectorTask(TASK_ID, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                    INITIAL_VECTOR_SIZE, op));
        }


        long addTime = operationTasks.get(Operation.ADD).getExecutionTime();
        assertTrue(operationTasks.get(Operation.MUL).getExecutionTime() > addTime,
                "MUL should take longer than ADD");
        assertTrue(operationTasks.get(Operation.REDUCE).getExecutionTime() >
                        operationTasks.get(Operation.MUL).getExecutionTime(),
                "REDUCE should take longest");
    }


    @Test
    @DisplayName("Task should handle concurrent vector operations")
    void testConcurrentOperations() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();


        for (Operation op : Operation.values()) {
            results.add(executor.submit(() -> {
                startLatch.await();
                VectorTask vectorTask = new VectorTask(TASK_ID, INITIAL_PRIORITY,
                        INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                        INITIAL_VECTOR_SIZE, op);
                vectorTask.execute();
                vectorTask.optimize();
                return vectorTask.isOptimized();
            }));
        }

        startLatch.countDown();


        for (Future<Boolean> result : results) {
            assertTrue(result.get(5, TimeUnit.SECONDS));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }


    @Test
    @DisplayName("Task should handle vector resource scaling")
    void testResourceScaling() {
        int[] vectorSizes = {128, 256, 512, 1024};
        Operation[] operations = Operation.values();
        Map<String, Long> executionTimes = new HashMap<>();


        for (int size : vectorSizes) {
            for (Operation op : operations) {
                task.setVectorSize(size);
                task.setVectorOperation(op);
                String key = String.format("size=%d,op=%s", size, op);
                executionTimes.put(key, task.getExecutionTime());
            }
        }


        for (int i = 1; i < vectorSizes.length; i++) {
            for (Operation op : operations) {
                String key1 = String.format("size=%d,op=%s", vectorSizes[i-1], op);
                String key2 = String.format("size=%d,op=%s", vectorSizes[i], op);
                assertTrue(executionTimes.get(key2) > executionTimes.get(key1),
                        "Larger vector size should increase execution time");
            }
        }
    }


    @Test
    @DisplayName("Task should optimize performance under different conditions")
    void testPerformanceOptimization() {
        task.optimize();
        assertTrue(task.isOptimized(), "Main task instance should be optimized");

        for (Operation op : Operation.values()) {
            VectorTask testTask = new VectorTask(TASK_ID, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                    INITIAL_VECTOR_SIZE, op);

            long baseTime = testTask.getExecutionTime();

            testTask.optimize();
            long optimizedTime = testTask.getExecutionTime();

            assertTrue(optimizedTime < baseTime,
                    String.format("Optimized time (%d) should be less than base time (%d) for %s",
                            optimizedTime, baseTime, op));

            long expectedTime = (long)(baseTime * 0.7);
            assertEquals(expectedTime, optimizedTime, baseTime * 0.1,
                    String.format("Operation %s: Expected optimized time around %d, got %d (base: %d)",
                            op, expectedTime, optimizedTime, baseTime));
        }

        int originalSize = task.getVectorSize();
        task.setVectorSize(originalSize * 2);
        assertTrue(task.isOptimized(),
                "Optimization state should persist after vector size change");

        Operation originalOp = task.getVectorOperation();
        task.setVectorOperation(Operation.MUL);
        assertTrue(task.isOptimized(),
                "Optimization state should persist after operation change");
        task.setVectorSize(originalSize);
        task.setVectorOperation(originalOp);
    }

    @Test
    @DisplayName("Task should handle sustained vector operations")
    void testSustainedOperations() {
        int iterations = 1000;
        Operation[] operations = Operation.values();
        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            Operation op = operations[i % operations.length];
            VectorTask loadTask = new VectorTask(i, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE,
                    INITIAL_VECTOR_SIZE, op);
            executionTimes.add(loadTask.getExecutionTime());
            loadTask.execute();
            assertEquals(Status.RUNNING, loadTask.getStatus());
        }


        assertTrue(executionTimes.stream().allMatch(time -> time > 0),
                "All execution times should be positive");
    }


    @ParameterizedTest
    @CsvSource({
            "ADD, 512, 2, 8",
            "MUL, 1024, 4, 16",
            "REDUCE, 2048, 8, 32"
    })
    @DisplayName("Task should handle different vector configurations")
    void testVectorConfigurations(Operation op, int vectorSize,
                                  int computeUnits, int batchSize) {
        VectorTask configTask = new VectorTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, computeUnits, batchSize, vectorSize, op);

        long baseTime = configTask.getExecutionTime();
        configTask.optimize();

        assertTrue(configTask.getExecutionTime() < baseTime,
                "Optimized time should be less than base time");
        assertEquals(Status.PENDING, configTask.getStatus());
    }
}