package task.sync;

import status.Sync;
import task.NPUTask;

public class SyncTask extends NPUTask {
    private double frequency;
    private int bufferSize;
    private final Sync mode;
    private int latency;
    private double voltageScale;

    private static final int MAX_BUFFER_SIZE = 256;
    private static final int MAX_LATENCY = 10000;

    public SyncTask(long taskId, int priority, int memorySize,
                    double frequency, int bufferSize, Sync mode) {
        super(taskId, priority, memorySize);
        this.mode = mode;
        setFrequency(frequency);
        setBufferSize(bufferSize);
        this.latency = 1;
        this.voltageScale = 1.0;
        calculateExecutionTime();
    }

    @Override
    protected void calculateExecutionTime() {
        if (mode == null) {
            super.calculateExecutionTime();
            return;
        }

        super.calculateExecutionTime();
        double baseTime = this.executionTime;

        // Simplified calculation with bounded factors
        double frequencyFactor = Math.min(1000.0 / frequency, 10.0);
        double bufferFactor = Math.min(bufferSize / 64.0, 4.0);
        double modeFactor = getModeFactor();

        this.executionTime = (long)(baseTime * frequencyFactor * bufferFactor * modeFactor);
    }

    private double getModeFactor() {
        return switch(mode) {
            case BARRIER -> 2.0;
            case PIPELINE -> 1.2;
            case WAVEFRONT -> 1.5;
            case ASYNC -> 1.0;
        };
    }

    // Getters and setters with bounds checking
    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) {
        this.frequency = Math.max(1.0, frequency);
        calculateExecutionTime();
    }

    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int size) {
        this.bufferSize = Math.max(1, Math.min(size, MAX_BUFFER_SIZE));
        calculateExecutionTime();
    }

    public Sync getMode() { return mode; }

    public int getLatency() { return latency; }
    public void setLatency(int latency) {
        this.latency = Math.max(1, Math.min(latency, MAX_LATENCY));
        calculateExecutionTime();
    }

    public double getVoltageScale() { return voltageScale; }
    public void setVoltageScale(double scale) {
        this.voltageScale = Math.max(0.1, Math.min(2.0, scale));
        calculateExecutionTime();
    }
}