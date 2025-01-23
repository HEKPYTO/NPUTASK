package task;

import status.Memory;

public class MemoryTask extends NPUTask {
    private int bandwidth;
    private final Memory memoryType;

    public MemoryTask(long taskId, int priority, int memorySize, int bandwidth, Memory memoryType) {
        super(taskId, priority, memorySize);
        this.bandwidth = bandwidth;
        this.memoryType = memoryType;
        calculateExecutionTime();
    }

    @Override
    protected void calculateExecutionTime() {
        super.calculateExecutionTime();

        if (memoryType == null) {
            return;
        }

        double bandwidthFactor = 1000.0 / bandwidth;
        double typeMultiplier = getMemoryTypeMultiplier();
        this.executionTime = (long)(executionTime * bandwidthFactor * typeMultiplier);
    }

    private double getMemoryTypeMultiplier() {
        return switch (memoryType) {
            case CACHE -> 0.5;
            case RAM -> 1.0;
            case VRAM -> 1.5;
            case DISK -> 5.00;
        };
    }

    public int getBandwidth() { return bandwidth; }
    public void setBandwidth(int bandwidth) {
        this.bandwidth = Math.max(1, bandwidth);
        calculateExecutionTime();
    }

    public Memory getMemoryType() {
        return memoryType;
    }
}