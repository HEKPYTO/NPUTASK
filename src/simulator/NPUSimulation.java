package simulator;

import task.NPUTask;
import task.ComputationTask;
import task.TensorTask;
import task.VectorTask;
import task.MemoryTask;
import kernel.TaskExecutor;
import status.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class NPUSimulation {
    private static final TaskExecutor executor = TaskExecutor.getInstance();
    private static long taskIdCounter = 1;

    public static void main(String[] args) {
        System.out.println("=== Starting NPU Workload Simulation ===\n");

        simulateMLTrainingWorkload();

        simulateDataTransferWorkload();

        simulateMixedWorkload();

        executor.shutdown();
        System.out.println("\n=== Simulation Complete ===");
    }

    private static void simulateMLTrainingWorkload() {
        System.out.println("--- ML Training Workload ---");

        // Create a tensor task for matrix multiplication
        TensorTask tensorTask = new TensorTask(
                getNextTaskId(),
                120,  // High priority
                4096, // Large memory size
                8,    // Multiple compute units
                32,   // Large batch size
                new int[]{256, 256, 3},  // 3D tensor dimensions
                Quantization.FLOAT32     // High precision
        );

        System.out.println("\nInitiating Tensor Task:");
        System.out.printf("Task ID: %d, Dimensions: %s, Type: %s\n",
                tensorTask.getTaskId(),
                Arrays.toString(tensorTask.getDimensions()),
                tensorTask.getTensorType());
        System.out.printf("Estimated execution time: %d ms\n", tensorTask.getExecutionTime());

        tensorTask.execute();
        waitForTask(tensorTask);

        // Create vector operations
        VectorTask[] vectorTasks = new VectorTask[3];
        Operation[] operations = {Operation.ADD, Operation.MUL, Operation.REDUCE};

        System.out.println("\nInitiating Vector Operations:");
        for (int i = 0; i < operations.length; i++) {
            vectorTasks[i] = new VectorTask(
                    getNextTaskId(),
                    110,    // Medium priority
                    1024,   // Medium memory size
                    4,      // Standard compute units
                    16,     // Standard batch size
                    2048,   // Large vector size
                    operations[i]
            );

            System.out.printf("\nVector Task %d:\n", i + 1);
            System.out.printf("Task ID: %d, Operation: %s, Vector Size: %d\n",
                    vectorTasks[i].getTaskId(),
                    vectorTasks[i].getVectorOperation(),
                    vectorTasks[i].getVectorSize());
            System.out.printf("Estimated execution time: %d ms\n", vectorTasks[i].getExecutionTime());

            vectorTasks[i].execute();
            waitForTask(vectorTasks[i]);
        }
    }

    private static void simulateDataTransferWorkload() {
        System.out.println("\n--- Data Transfer Workload ---");

        Memory[] memoryTypes = {Memory.CACHE, Memory.RAM, Memory.VRAM, Memory.DISK};
        MemoryTask[] memoryTasks = new MemoryTask[memoryTypes.length];

        System.out.println("\nInitiating Memory Transfers:");
        for (int i = 0; i < memoryTypes.length; i++) {
            memoryTasks[i] = new MemoryTask(
                    getNextTaskId(),
                    115,    // Medium-high priority
                    8192,   // Large memory size
                    1000,   // Standard bandwidth
                    memoryTypes[i]
            );

            System.out.printf("\nMemory Task %d:\n", i + 1);
            System.out.printf("Task ID: %d, Memory Type: %s, Bandwidth: %d\n",
                    memoryTasks[i].getTaskId(),
                    memoryTasks[i].getMemoryType(),
                    memoryTasks[i].getBandwidth());
            System.out.printf("Estimated execution time: %d ms\n", memoryTasks[i].getExecutionTime());

            memoryTasks[i].execute();
            waitForTask(memoryTasks[i]);
        }
    }

    private static void simulateMixedWorkload() {
        System.out.println("\n--- Mixed Workload ---");

        ComputationTask compTask = new ComputationTask(
                getNextTaskId(),
                125,    // High priority
                2048,   // Medium memory size
                8,      // Multiple compute units
                32      // Large batch size
        );

        System.out.println("\nInitiating Computation Task:");
        System.out.printf("Task ID: %d, Compute Units: %d, Efficiency: %.2f\n",
                compTask.getTaskId(),
                compTask.getComputeUnits(),
                compTask.getEfficiency());
        System.out.printf("Estimated execution time: %d ms\n", compTask.getExecutionTime());

        compTask.execute();
        waitForTask(compTask);
    }

    private static void waitForTask(NPUTask task) {
        try {
            // Wait for task completion
            while (executor.isTaskRunning(String.valueOf(task.getTaskId()))) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            double basePower = getBasePower(task);

            task.setPowerConsumption(basePower);
            System.out.printf("Task %d completed with status: %s\n",
                    task.getTaskId(), Status.COMPLETED);
            System.out.printf("Power consumption: %.2f units\n", task.getPowerConsumption());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("Task %d interrupted\n", task.getTaskId());
        }
    }

    private static double getBasePower(NPUTask task) {
        double basePower = task.getPowerConsumption();
        switch (task) {
            case TensorTask t -> basePower *= (t.getTensorType() == Quantization.FLOAT32 ? 1.5 : 1.0);
            case VectorTask v -> basePower *= switch (v.getVectorOperation()) {
                case ADD -> 1.0;
                case MUL -> 1.2;
                case REDUCE -> 1.5;
            };
            case MemoryTask m -> basePower *= switch (m.getMemoryType()) {
                case CACHE -> 0.5;
                case RAM -> 1.0;
                case VRAM -> 1.5;
                case DISK -> 2.0;
            };
            default -> {
            }
        }
        return basePower;
    }

    private static long getNextTaskId() {
        return taskIdCounter++;
    }
}