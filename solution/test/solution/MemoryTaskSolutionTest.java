package test.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Memory;
import status.Status;
import task.MemoryTask;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTaskSolutionTest {
    private MemoryTask task;
    private static final long TASK_ID = 9876L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final int INITIAL_BANDWIDTH = 1000;
    private static final Memory INITIAL_MEMORY_TYPE = Memory.RAM;

    @BeforeEach
    void setUp() {
        task = new MemoryTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_BANDWIDTH, INITIAL_MEMORY_TYPE);
    }

    @Test
    @DisplayName("Task should handle memory hierarchy operations")
    void testMemoryHierarchyOperations() {

        Map<Memory, MemoryTask> memoryTasks = new EnumMap<>(Memory.class);
        for (Memory type : Memory.values()) {
            memoryTasks.put(type, new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, type));
        }

        long ramTime = memoryTasks.get(Memory.RAM).getExecutionTime();
        assertTrue(memoryTasks.get(Memory.CACHE).getExecutionTime() < ramTime,
                "Cache should be faster than RAM");
        assertTrue(memoryTasks.get(Memory.VRAM).getExecutionTime() > ramTime,
                "VRAM should be slower than RAM");
        assertTrue(memoryTasks.get(Memory.DISK).getExecutionTime() >
                        memoryTasks.get(Memory.VRAM).getExecutionTime(),
                "Disk should be slowest");
    }

    @Test
    @DisplayName("Task should handle concurrent memory operations")
    void testConcurrentMemoryAccess() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Long>> results = new ArrayList<>();


        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> {
                startLatch.await();
                MemoryTask memTask = new MemoryTask(TASK_ID + threadCount,
                        INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                        INITIAL_BANDWIDTH, INITIAL_MEMORY_TYPE);
                memTask.execute();
                return memTask.getExecutionTime();
            }));
        }

        startLatch.countDown();


        for (Future<Long> result : results) {
            assertTrue(result.get(5, TimeUnit.SECONDS) > 0);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Task should scale memory performance correctly")
    void testMemoryPerformanceScaling() {

        int[] bandwidths = {500, 1000, 2000, 4000};
        List<Long> executionTimes = new ArrayList<>();

        for (int bandwidth : bandwidths) {
            task.setBandwidth(bandwidth);
            executionTimes.add(task.getExecutionTime());
        }


        for (int i = 1; i < executionTimes.size(); i++) {
            assertTrue(executionTimes.get(i) < executionTimes.get(i-1),
                    "Execution time should decrease with higher bandwidth");
        }
    }

    @Test
    @DisplayName("Task should handle different memory type characteristics")
    void testMemoryTypeCharacteristics() {
        Memory[] types = Memory.values();
        int[] sizes = {1024, 2048, 4096};
        int[] bandwidths = {500, 1000, 2000};

        for (Memory type : types) {
            for (int size : sizes) {
                for (int bandwidth : bandwidths) {
                    MemoryTask testTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                            size, bandwidth, type);
                    assertTrue(testTask.getExecutionTime() > 0);
                    testTask.execute();
                    assertEquals(Status.RUNNING, testTask.getStatus());
                }
            }
        }
    }

    @Test
    @DisplayName("Task should handle extreme memory conditions")
    void testExtremeMemoryConditions() {

        MemoryTask minTask = new MemoryTask(TASK_ID, 100, 0, 1, Memory.CACHE);
        assertTrue(minTask.getExecutionTime() > 0);


        MemoryTask maxTask = new MemoryTask(TASK_ID, 139, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Memory.DISK);
        assertTrue(maxTask.getExecutionTime() > 0);
        assertTrue(maxTask.getExecutionTime() < Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Task should handle sustained memory operations")
    void testSustainedMemoryOperations() {
        int iterations = 1000;
        Memory[] types = Memory.values();

        for (int i = 0; i < iterations; i++) {
            Memory type = types[i % types.length];
            MemoryTask loadTask = new MemoryTask(i, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, type);
            loadTask.execute();
            assertEquals(Status.RUNNING, loadTask.getStatus());
            assertTrue(loadTask.getExecutionTime() > 0);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "CACHE, 500, 1024",
            "RAM, 1000, 4096",
            "VRAM, 2000, 8192",
            "DISK, 100, 16384"
    })
    @DisplayName("Task should handle different memory configurations")
    void testMemoryConfigurations(Memory type, int bandwidth, int size) {
        MemoryTask configTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                size, bandwidth, type);

        long execTime = configTask.getExecutionTime();
        configTask.execute();

        assertTrue(execTime > 0);
        assertEquals(Status.RUNNING, configTask.getStatus());
    }


    @Test
    @DisplayName("Task should handle memory operation failures gracefully")
    void testMemoryOperationFailure() {
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.setStatus(Status.FAILED);
        assertEquals(Status.FAILED, task.getStatus());

        task.execute();
        assertEquals(Status.FAILED, task.getStatus());
    }
}