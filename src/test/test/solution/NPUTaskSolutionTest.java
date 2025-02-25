package test.test.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Status;
import task.NPUTask;

import static org.junit.jupiter.api.Assertions.*;

public class NPUTaskSolutionTest {
    private NPUTask task;
    private static final long TASK_ID = 9876L;
    private static final int INITIAL_PRIORITY = 115;
    private static final int INITIAL_MEMORY_SIZE = 1024;

    @BeforeEach
    void setUp() {
        task = new NPUTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
    }

    @ParameterizedTest
    @CsvSource({
            "105, 512, 537.6",    // Lower case
            "125, 2048, 2560.0",  // Middle case
            "135, 8192, 11059.2"  // Higher case
    })
    @DisplayName("Power consumption should be calculated correctly")
    void testPowerConsumptionCalculation(int priority, int memorySize, double expectedPower) {
        NPUTask powerTask = new NPUTask(2L, priority, memorySize);
        powerTask.execute();
        assertEquals(expectedPower, powerTask.getPowerConsumption(), 0.01);
    }

    @Test
    @DisplayName("Execute should only change status if PENDING")
    void testExecuteStatusChange() {
        assertEquals(Status.PENDING, task.getStatus());
        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.execute();
        assertEquals(Status.RUNNING, task.getStatus());

        task.setStatus(Status.COMPLETED);
        task.execute();
        assertEquals(Status.COMPLETED, task.getStatus());

        task.setStatus(Status.FAILED);
        task.execute();
        assertEquals(Status.FAILED, task.getStatus());
    }
}