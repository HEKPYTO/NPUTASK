package task;

import kernel.TaskExecutor;
import status.Status;

public class NPUTask {
    private final long taskId;
    private int priority;
    private long memorySize;
    private Status status;
    private double powerConsumption;
    protected long executionTime;

    public NPUTask(long taskId, int priority, int memorySize) {
        this.taskId = taskId;
        setPriority(priority);
        setMemorySize(memorySize);
        setPowerConsumption(0.0);
        setStatus(Status.PENDING);
        calculateExecutionTime();
    }

    public void execute() {
        if (status == Status.PENDING) {
            status = Status.RUNNING;
            calculatePowerConsumption();
            TaskExecutor.getInstance().executeTask(this);
        }
    }

    protected void calculateExecutionTime() {
        long baseTime = 100;
        double priorityFactor = (priority - 100) / 39.0;
        double memoryFactor = memorySize / 1024.0;
        this.executionTime = (long)(baseTime * (1 + priorityFactor) * (1 + memoryFactor));
    }

    protected void calculatePowerConsumption() {
        double value = memorySize * 0.01 * priority;
        setPowerConsumption(value);
    }

    public long getTaskId() { return taskId; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) {
        this.priority = Math.min(139, Math.max(100, priority));
        calculateExecutionTime();
    }

    public long getExecutionTime() { return executionTime; }

    public long getMemorySize() { return memorySize; }
    public void setMemorySize(int memorySize) {
        this.memorySize = Math.max(memorySize, 0);
        calculateExecutionTime();
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public double getPowerConsumption() { return powerConsumption; }
    public void setPowerConsumption(double powerConsumption) {
        this.powerConsumption = Math.max(powerConsumption, 0);
        calculateExecutionTime();
    }
}