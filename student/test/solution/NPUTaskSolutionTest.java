// Solution Test Class (sol)
package test.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Status;
import task.NPUTask;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NPUTaskSolutionTest {
    private NPUTask task;
    private static final long TASK_ID = 9876L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;

    @BeforeEach
    void setUp() {
        task = new NPUTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
    }

    @Test
    @DisplayName("Task should handle concurrent execution attempts")
    void testConcurrentExecution() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    task.execute();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        startLatch.countDown();
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(Status.RUNNING, task.getStatus());
        assertTrue(task.getPowerConsumption() > 0);
    }

    @Test
    @DisplayName("Task should handle resource allocation and cleanup")
    void testResourceManagement() {
        task.setMemorySize(Integer.MAX_VALUE / 2);
        task.setPriority(139);

        task.execute();
        assertTrue(task.getExecutionTime() > 0);
        assertTrue(task.getPowerConsumption() > 0);

        task.setStatus(Status.COMPLETED);
        assertEquals(Status.COMPLETED, task.getStatus());
    }

    @Test
    @DisplayName("Task should maintain consistent state during execution")
    void testStateConsistency() {
        long initialExecTime = task.getExecutionTime();
        Status initialStatus = task.getStatus();

        task.execute();


        assertEquals(initialExecTime, task.getExecutionTime());
        assertNotEquals(initialStatus, task.getStatus());
        assertTrue(task.getPowerConsumption() > 0);


        task.setPriority(130);
        assertNotEquals(initialExecTime, task.getExecutionTime());
    }


    @ParameterizedTest
    @CsvSource({
            "100, 1024",
            "120, 16384",
            "139, 65536"
    })
    @DisplayName("Task should scale performance with configuration")
    void testPerformanceScaling(int priority, int memorySize) {
        task.setPriority(priority);
        task.setMemorySize(memorySize);

        long execTime = task.getExecutionTime();
        task.execute();

        assertTrue(execTime > 0);
        assertTrue(task.getPowerConsumption() > 0);
        assertTrue(task.getPowerConsumption() < Double.MAX_VALUE);
    }


    @Test
    @DisplayName("Task should handle execution failures gracefully")
    void testFailureHandling() {

        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());


        task.setStatus(Status.FAILED);
        assertEquals(Status.FAILED, task.getStatus());


        task.execute();
        assertEquals(Status.FAILED, task.getStatus());
        assertTrue(task.getPowerConsumption() > 0);
    }


    @Test
    @DisplayName("Task should handle repeated execution attempts")
    void testRepeatedExecution() {
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            NPUTask loadTask = new NPUTask(i, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
            loadTask.execute();
            assertTrue(loadTask.getExecutionTime() > 0);
            assertTrue(loadTask.getPowerConsumption() > 0);
            assertEquals(Status.RUNNING, loadTask.getStatus());
        }
    }


    @Test
    @DisplayName("Task should handle priority and memory size interactions correctly")
    void testPriorityMemoryInteraction() {

        int[] priorities = {100, 120, 139};
        int[] memorySizes = {1024, 4096, 16384};

        for (int priority : priorities) {
            for (int memorySize : memorySizes) {
                task.setPriority(priority);
                task.setMemorySize(memorySize);

                long execTime = task.getExecutionTime();
                task.execute();

                assertTrue(execTime > 0);
                assertTrue(task.getPowerConsumption() > 0);
                assertEquals(Status.RUNNING, task.getStatus());
            }
        }
    }
}