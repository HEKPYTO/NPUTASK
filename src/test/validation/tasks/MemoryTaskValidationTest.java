package test.validation.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import status.Memory;
import status.Status;
import task.memory.MemoryTask;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTaskValidationTest {
    private MemoryTask task;
    private static final long TASK_ID = 12345L;
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
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructorInitialization() {
        assertEquals(TASK_ID, task.getTaskId());
        assertEquals(INITIAL_PRIORITY, task.getPriority());
        assertEquals(INITIAL_MEMORY_SIZE, task.getMemorySize());
        assertEquals(INITIAL_BANDWIDTH, task.getBandwidth());
        assertEquals(INITIAL_MEMORY_TYPE, task.getMemoryType());
        assertEquals(Status.PENDING, task.getStatus());
    }

    @Test
    @DisplayName("Constructor should handle null memory type")
    void testConstructorWithNullMemoryType() {
        MemoryTask nullTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, null);
        assertNull(nullTask.getMemoryType());
        assertTrue(nullTask.getExecutionTime() > 0);
    }

    @ParameterizedTest
    @EnumSource(Memory.class)
    @DisplayName("All memory types should be accepted")
    void testValidMemoryTypes(Memory memoryType) {
        MemoryTask typeTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, memoryType);
        assertEquals(memoryType, typeTask.getMemoryType());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
    @DisplayName("Bandwidth should not be less than 1")
    void testInvalidBandwidth(int invalidBandwidth) {
        task.setBandwidth(invalidBandwidth);
        assertEquals(1, task.getBandwidth(),
                "Bandwidth should be set to minimum value of 1");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 500, 1000, 2000, Integer.MAX_VALUE})
    @DisplayName("Valid bandwidth values should be accepted")
    void testValidBandwidth(int validBandwidth) {
        task.setBandwidth(validBandwidth);
        assertEquals(validBandwidth, task.getBandwidth());
    }

    @ParameterizedTest
    @CsvSource({
            "CACHE, 0.5",
            "RAM, 1.0",
            "VRAM, 1.5",
            "DISK, 5.0"
    })
    @DisplayName("Memory type should affect execution time correctly")
    void testMemoryTypeMultiplier(Memory memoryType, double expectedMultiplier) {
        MemoryTask referenceTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, Memory.RAM);
        long referenceTime = referenceTask.getExecutionTime();

        MemoryTask testTask = new MemoryTask(TASK_ID, INITIAL_PRIORITY,
                INITIAL_MEMORY_SIZE, INITIAL_BANDWIDTH, memoryType);
        long actualTime = testTask.getExecutionTime();

        assertEquals(referenceTime * expectedMultiplier, actualTime, 1.0,
                String.format("Memory type %s should multiply execution time by %.1f",
                        memoryType, expectedMultiplier));
    }

    @Test
    @DisplayName("Bandwidth should affect execution time inversely")
    void testBandwidthImpact() {
        long baseTime = task.getExecutionTime();

        task.setBandwidth(INITIAL_BANDWIDTH * 2);
        assertEquals(baseTime / 2.0, task.getExecutionTime(), 1.0,
                "Doubling bandwidth should halve execution time");

        task.setBandwidth(INITIAL_BANDWIDTH / 2);
        assertEquals(baseTime * 2.0, task.getExecutionTime(), 1.0,
                "Halving bandwidth should double execution time");
    }

    @Test
    @DisplayName("Task should maintain inherited NPUTask behavior")
    void testInheritedBehavior() {
        assertEquals(Status.PENDING, task.getStatus());
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.setPriority(150);
        assertEquals(139, task.getPriority());
        task.setPriority(90);
        assertEquals(100, task.getPriority());

        task.setMemorySize(-1024);
        assertEquals(0, task.getMemorySize());
    }
}