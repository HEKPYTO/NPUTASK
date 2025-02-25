package test.test.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import status.Status;
import task.NPUTask;

import static org.junit.jupiter.api.Assertions.*;

public class NPUTaskValidationTest {
    private NPUTask task;
    private static final long TASK_ID = 12345L;
    private static final int INITIAL_PRIORITY = 120;
    private static final int INITIAL_MEMORY_SIZE = 2048;

    @BeforeEach
    void setUp() {
        task = new NPUTask(TASK_ID, INITIAL_PRIORITY, INITIAL_MEMORY_SIZE);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 1024, 1024.0",  // Minimum priority
            "120, 2048, 2457.6",  // Medium case
            "139, 4096, 5693.44"  // Maximum case
    })
    @DisplayName("Power consumption should be calculated correctly")
    void testPowerConsumptionCalculation(int priority, int memorySize, double expectedPower) {
        NPUTask powerTask = new NPUTask(1L, priority, memorySize);
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

