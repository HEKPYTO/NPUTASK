package kernel;

import task.NPUTask;
import status.Status;
import java.util.concurrent.*;
import java.util.*;

public class TaskExecutor {
    private static final TaskExecutor INSTANCE = new TaskExecutor();
    private final ExecutorService executor;
    private final Map<Long, Future<?>> runningTasks;

    private TaskExecutor() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.runningTasks = new ConcurrentHashMap<>();
    }

    public static TaskExecutor getInstance() {
        return INSTANCE;
    }

    public void executeTask(NPUTask task) {
        Future<?> future = executor.submit(() -> {
            try {
                Thread.sleep(task.getExecutionTime());
                task.setStatus(Status.COMPLETED);

            } catch (InterruptedException e) {
                task.setStatus(Status.FAILED);
                Thread.currentThread().interrupt();
            } finally {
                runningTasks.remove(task.getTaskId());
            }
        });

        runningTasks.put(task.getTaskId(), future);
    }

    public void cancelTask(String taskId) {
        Future<?> future = runningTasks.get(taskId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public boolean isTaskRunning(String taskId) {
        Future<?> future = runningTasks.get(taskId);
        return future != null && !future.isDone();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}