package test.solution.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Sync;
import status.Status;
import task.sync.SyncTask;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SyncTaskSolutionTest {
    private SyncTask task;
    private static final long TASK_ID = 9876L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final double INITIAL_FREQUENCY = 1000.0;
    private static final int INITIAL_BUFFER_SIZE = 64;
    private static final Sync INITIAL_MODE = Sync.BARRIER;

    @BeforeEach
    void setUp() {
        task = new SyncTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, INITIAL_MODE);
    }

    @Test
    @DisplayName("Task should handle different sync modes correctly")
    void testSyncModeOperations() {
        Map<Sync, SyncTask> syncTasks = new EnumMap<>(Sync.class);


        for (Sync mode : Sync.values()) {
            syncTasks.put(mode, new SyncTask(TASK_ID, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, mode));
        }


        long asyncTime = syncTasks.get(Sync.ASYNC).getExecutionTime();
        assertTrue(syncTasks.get(Sync.PIPELINE).getExecutionTime() > asyncTime);
        assertTrue(syncTasks.get(Sync.WAVEFRONT).getExecutionTime() >
                syncTasks.get(Sync.PIPELINE).getExecutionTime());
        assertTrue(syncTasks.get(Sync.BARRIER).getExecutionTime() >
                syncTasks.get(Sync.WAVEFRONT).getExecutionTime());
    }

    @Test
    @DisplayName("Task should handle concurrent sync operations")
    void testConcurrentSyncOperations() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Double>> results = new ArrayList<>();


        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            results.add(executor.submit(() -> {
                startLatch.await();
                SyncTask syncTask = new SyncTask(TASK_ID + finalI, INITIAL_PRIORITY,
                        INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY * (finalI + 1),
                        INITIAL_BUFFER_SIZE, INITIAL_MODE);
                syncTask.execute();
                return syncTask.getVoltageScale();
            }));
        }

        startLatch.countDown();


        for (Future<Double> result : results) {
            assertEquals(1.0, result.get(5, TimeUnit.SECONDS));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Task should handle extreme resource conditions")
    void testExtremeConditions() {
        SyncTask minTask = new SyncTask(TASK_ID, 100, 1024,
                1000.0,
                32,
                Sync.ASYNC);
        long minTime = minTask.getExecutionTime();
        assertTrue(minTime > 0, "Minimum configuration should have positive execution time");
        assertTrue(minTask.getVoltageScale() >= 0.1, "Voltage scale should be at least 0.1");

        SyncTask maxTask = new SyncTask(TASK_ID, 139, 4096,
                500.0,
                256,
                Sync.BARRIER);
        long maxTime = maxTask.getExecutionTime();

        assertTrue(maxTime > minTime,
                String.format("Maximum config time (%d) should exceed minimum config time (%d)",
                        maxTime, minTime));
    }
    @Test
    @DisplayName("Task should handle combined parameter effects")
    void testCombinedEffects() {

        long baseTime = task.getExecutionTime();


        task.setPriority(139);
        task.setMemorySize(INITIAL_MEMORY_SIZE * 2);
        task.setFrequency(INITIAL_FREQUENCY / 2);
        task.setBufferSize(INITIAL_BUFFER_SIZE * 2);
        task.setLatency(5);
        task.setVoltageScale(1.5);

        long modifiedTime = task.getExecutionTime();
        assertTrue(modifiedTime != baseTime,
                String.format("Execution time should change (base: %d, modified: %d)",
                        baseTime, modifiedTime));
    }

    @Test
    @DisplayName("Task should handle clock domain interactions")
    void testClockDomainInteractions() {

        double[] frequencies = {500.0, 1000.0, 2000.0};
        double[] voltages = {0.5, 1.0, 1.5};


        SyncTask referenceTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, 1000.0, INITIAL_BUFFER_SIZE, INITIAL_MODE);
        long referenceTime = referenceTask.getExecutionTime();

        for (double freq : frequencies) {
            SyncTask testTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, freq, INITIAL_BUFFER_SIZE, INITIAL_MODE);
            long freqTime = testTask.getExecutionTime();


            if (freq < 1000.0) {
                assertTrue(freqTime > referenceTime,
                        String.format("Lower frequency %.1f should increase execution time (ref: %d, actual: %d)",
                                freq, referenceTime, freqTime));
            } else if (freq > 1000.0) {
                assertTrue(freqTime < referenceTime,
                        String.format("Higher frequency %.1f should decrease execution time (ref: %d, actual: %d)",
                                freq, referenceTime, freqTime));
            }
        }
    }

    @Test
    @DisplayName("Task should handle buffer management scenarios")
    void testBufferManagement() {
        int[] bufferSizes = {32, 64, 128, 256};
        Map<Integer, Long> executionTimes = new HashMap<>();


        for (int size : bufferSizes) {
            task.setBufferSize(size);
            executionTimes.put(size, task.getExecutionTime());
        }


        for (int i = 1; i < bufferSizes.length; i++) {
            assertNotEquals(executionTimes.get(bufferSizes[i-1]),
                    executionTimes.get(bufferSizes[i]),
                    "Different buffer sizes should affect execution time");
        }


        task.setBufferSize(512);
        assertEquals(256, task.getBufferSize());
    }

    @Test
    @DisplayName("Task should handle sustained sync operations")
    void testSustainedOperations() {
        int iterations = 1000;
        Sync[] modes = Sync.values();
        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            Sync mode = modes[i % modes.length];
            SyncTask loadTask = new SyncTask(i, INITIAL_PRIORITY,
                    INITIAL_MEMORY_SIZE, INITIAL_FREQUENCY, INITIAL_BUFFER_SIZE, mode);
            executionTimes.add(loadTask.getExecutionTime());
            loadTask.execute();
            assertEquals(Status.RUNNING, loadTask.getStatus());
        }


        assertTrue(executionTimes.stream().allMatch(time -> time > 0),
                "All execution times should be positive");
    }

    @ParameterizedTest
    @CsvSource({
            "BARRIER, 500.0, 32, 0.5",
            "PIPELINE, 1000.0, 64, 1.0",
            "WAVEFRONT, 2000.0, 128, 1.5",
            "ASYNC, 4000.0, 256, 2.0"
    })
    @DisplayName("Task should handle different sync configurations")
    void testSyncConfigurations(Sync mode, double frequency,
                                int bufferSize, double voltageScale) {
        SyncTask configTask = new SyncTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, frequency, bufferSize, mode);
        configTask.setVoltageScale(voltageScale);

        assertTrue(configTask.getExecutionTime() > 0);
        configTask.execute();
        assertEquals(Status.RUNNING, configTask.getStatus());
    }
}