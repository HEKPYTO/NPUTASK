package test.built.tasks;

import task.memory.MemoryTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import status.Memory;
import status.Status;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTaskTest {
    private MemoryTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;
    private static final int INITIAL_BANDWIDTH = 1000;

    @BeforeEach
    void setUp() {
        task = new MemoryTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE,
                INITIAL_BANDWIDTH, Memory.RAM);
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructor() {
        assertNotNull(task);
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_BANDWIDTH, task.getBandwidth());
        assertEquals(Memory.RAM, task.getMemoryType());
        assertEquals(Status.PENDING, task.getStatus());
        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Execution time calculation should handle null memory type correctly")
    void testExecutionTimeWithNullMemoryType() {
        MemoryTask baseTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, Memory.RAM);
        long baseExecutionTime = baseTask.getExecutionTime();

        MemoryTask nullTypeTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, null);
        long nullTypeExecutionTime = nullTypeTask.getExecutionTime();

        assertTrue(baseExecutionTime > 0, "Base execution time should be positive");
        assertTrue(nullTypeExecutionTime > 0, "Null type execution time should be positive");

        nullTypeTask.setPriority(130);
        long higherPriorityTime = nullTypeTask.getExecutionTime();
        assertTrue(higherPriorityTime > nullTypeExecutionTime,
                String.format("Execution time should increase with higher priority: %d -> %d",
                        nullTypeExecutionTime, higherPriorityTime));

        nullTypeTask.setBandwidth(INITIAL_BANDWIDTH * 2);
        assertEquals(higherPriorityTime, nullTypeTask.getExecutionTime(),
                "Bandwidth should not affect execution time with null memory type");
    }

    @Test
    @DisplayName("Execution time should be recalculated properly after initialization")
    void testExecutionTimeRecalculation() {
        long initialExecutionTime = task.getExecutionTime();

        assertTrue(initialExecutionTime > 0, "Initial execution time should be positive");

        task.setPriority(130);
        task.setMemorySize(4096);
        task.setBandwidth(2000);

        long finalExecutionTime = task.getExecutionTime();
        assertNotEquals(initialExecutionTime, finalExecutionTime,
                "Execution time should change after parameter updates");
    }

    @Test
    @DisplayName("Constructor should handle boundary values correctly")
    void testConstructorBoundaryValues() {
        MemoryTask minTask = new MemoryTask(Long.MIN_VALUE, 100, 0, 1, Memory.RAM);
        assertTrue(minTask.getExecutionTime() > 0,
                "Execution time should be positive with minimum values");

        MemoryTask maxTask = new MemoryTask(Long.MAX_VALUE, 139, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Memory.DISK);
        assertTrue(maxTask.getExecutionTime() > 0,
                "Execution time should be positive with maximum values");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Bandwidth should not be less than 1")
    void testInvalidBandwidth(int invalidBandwidth) {
        task.setBandwidth(invalidBandwidth);
        assertEquals(1, task.getBandwidth());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 500, 1000, 2000})
    @DisplayName("Valid bandwidth values should be accepted")
    void testValidBandwidth(int validBandwidth) {
        task.setBandwidth(validBandwidth);
        assertEquals(validBandwidth, task.getBandwidth());
    }

    @Test
    @DisplayName("Memory type should affect execution time correctly")
    void testMemoryTypeImpact() {
        MemoryTask cacheTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, Memory.CACHE);
        MemoryTask ramTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, Memory.RAM);
        MemoryTask vramTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, Memory.VRAM);
        MemoryTask diskTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, Memory.DISK);

        long ramTime = ramTask.getExecutionTime();
        assertEquals(ramTime * 0.5, cacheTask.getExecutionTime(), 1.0);
        assertEquals(ramTime * 1.5, vramTask.getExecutionTime(), 1.0);
        assertEquals(ramTime * 5.0, diskTask.getExecutionTime(), 1.0);
    }

    @ParameterizedTest
    @CsvSource({
            "500, 2.0",
            "2000, 0.5",
            "1000, 1.0",
            "250, 4.0"
    })
    @DisplayName("Bandwidth should affect execution time correctly")
    void testBandwidthImpact(int bandwidth, double expectedFactor) {
        MemoryTask referenceTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, 1000, Memory.RAM);
        long referenceTime = referenceTask.getExecutionTime();

        MemoryTask testTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, bandwidth, Memory.RAM);
        assertEquals(referenceTime * expectedFactor, testTask.getExecutionTime(), 1.0);
    }

    @Test
    @DisplayName("Memory type should be immutable")
    void testMemoryTypeImmutability() {
        Memory initialType = task.getMemoryType();
        assertEquals(Memory.RAM, initialType);
    }

    @Test
    @DisplayName("Changing bandwidth should update execution time")
    void testBandwidthUpdatesExecutionTime() {
        long initialTime = task.getExecutionTime();
        task.setBandwidth(2000);
        assertEquals(initialTime / 2.0, task.getExecutionTime(), 1.0);
    }

    @Test
    @DisplayName("Execution time calculation should include inherited factors")
    void testExecutionTimeInheritance() {
        task.setPriority(130);
        assertTrue(task.getExecutionTime() > 0);

        task.setMemorySize(4096);
        assertTrue(task.getExecutionTime() > 0);
    }

    @Test
    @DisplayName("Task should maintain inherited status functionality")
    void testStatusInheritance() {
        assertEquals(Status.PENDING, task.getStatus());
        task.setStatus(Status.RUNNING);
        assertEquals(Status.RUNNING, task.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Memory.class)
    @DisplayName("Constructor should accept all memory types")
    void testConstructorWithAllMemoryTypes(Memory memoryType) {
        MemoryTask newTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, memoryType);
        assertEquals(memoryType, newTask.getMemoryType());
        assertTrue(newTask.getExecutionTime() > 0);
    }
}