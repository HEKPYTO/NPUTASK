package test.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Status;
import task.ComputationTask;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComputationTaskSolutionTest {
    private ComputationTask task;
    private static final long TASK_ID = 9876L;
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
    @DisplayName("Task should handle parallel computation requests")
    void testParallelComputation() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Double>> results = new ArrayList<>();


        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> {
                startLatch.await();
                task.execute();
                return task.getEfficiency();
            }));
        }

        startLatch.countDown();


        for (Future<Double> result : results) {
            assertTrue(result.get(5, TimeUnit.SECONDS) > 0);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(Status.RUNNING, task.getStatus());
    }


    @Test
    @DisplayName("Task should scale resources appropriately")
    void testResourceScaling() {

        double baselineEfficiency = task.getEfficiency();


        int newComputeUnits = task.getComputeUnits() * 2;
        int newBatchSize = task.getBatchSize() * 2;

        task.setComputeUnits(newComputeUnits);
        task.setBatchSize(newBatchSize);



        assertTrue(task.getEfficiency() > baselineEfficiency,
                String.format("Efficiency should increase from %.2f to %.2f",
                        baselineEfficiency, task.getEfficiency()));
    }


    @Test
    @DisplayName("Task should maintain consistent state during parameter changes")
    void testStateConsistency() {

        double initialEfficiency = task.getEfficiency();


        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());


        int newComputeUnits = task.getComputeUnits() * 2;
        task.setComputeUnits(newComputeUnits);


        assertTrue(task.getEfficiency() > initialEfficiency,
                "Efficiency should increase with more compute units");
        assertEquals(Status.RUNNING, task.getStatus());
    }


    @Test
    @DisplayName("Task should optimize performance based on resources")
    void testPerformanceOptimization() {
        List<Long> executionTimes = new ArrayList<>();
        List<Double> efficiencies = new ArrayList<>();


        int[] computeUnits = {1, 2, 4, 8, 16};
        int[] batchSizes = {2, 4, 8, 16, 32};

        for (int units : computeUnits) {
            for (int size : batchSizes) {
                task.setComputeUnits(units);
                task.setBatchSize(size);
                executionTimes.add(task.getExecutionTime());
                efficiencies.add(task.getEfficiency());
            }
        }


        assertTrue(executionTimes.stream().allMatch(time -> time > 0));
        assertTrue(efficiencies.stream().allMatch(eff -> eff > 0));
    }


    @Test
    @DisplayName("Task should handle sustained load")
    void testSustainedLoad() {
        int iterations = 1000;
        List<Double> efficiencies = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            ComputationTask loadTask = new ComputationTask(i, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_COMPUTE_UNITS, INITIAL_BATCH_SIZE);
            loadTask.execute();
            efficiencies.add(loadTask.getEfficiency());
        }


        assertTrue(efficiencies.stream().allMatch(eff -> eff > 0));
        assertEquals(iterations, efficiencies.size());
    }


    @Test
    @DisplayName("Task should handle extreme resource configurations")
    void testExtremeResources() {

        task.setComputeUnits(1);
        task.setBatchSize(2);
        assertTrue(task.getExecutionTime() > 0);
        assertTrue(task.getEfficiency() > 0);


        task.setComputeUnits(Integer.MAX_VALUE);
        task.setBatchSize(Integer.MAX_VALUE - 1);
        assertTrue(task.getExecutionTime() > 0);
        assertTrue(task.getEfficiency() < Double.MAX_VALUE);
    }


    @ParameterizedTest
    @CsvSource({
            "1, 2, 120, 1024",
            "8, 32, 130, 4096",
            "16, 64, 139, 8192"
    })
    @DisplayName("Task should handle combined resource changes")
    void testCombinedResources(int computeUnits, int batchSize, int priority, int memorySize) {
        task.setComputeUnits(computeUnits);
        task.setBatchSize(batchSize);
        task.setPriority(priority);
        task.setMemorySize(memorySize);

        assertTrue(task.getExecutionTime() > 0);
        assertTrue(task.getEfficiency() > 0);
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());
    }
}