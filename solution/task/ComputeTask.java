package task;

public class ComputeTask extends NPUTask {
    private int computeUnits;
    private int batchSize;
    private double efficiency;

    public ComputeTask(long taskId, int priority, int memorySize, int computeUnits, int batchSize) {
        super(taskId, priority, memorySize);
        setComputeUnits(computeUnits);
        setBatchSize(batchSize);
        calculateEfficiency();
    }

    @Override
    protected void calculateExecutionTime() {
        super.calculateExecutionTime();

        double computeFactor = 1.0 / computeUnits;
        double batchFactor = batchSize / 16.0;
        this.executionTime = (long) (executionTime * computeFactor * batchFactor);
    }

    private void calculateEfficiency() {
        this.efficiency = (computeUnits * batchSize) / 100.0;
    }

    public int getComputeUnits() { return computeUnits; }
    public void setComputeUnits(int units) {
        this.computeUnits = Math.max(1, units);
        calculateExecutionTime();
        calculateEfficiency();
    }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int size) {
        this.batchSize = Math.max(2, (size / 2) * 2);
        calculateExecutionTime();
        calculateEfficiency();
    }

    public double getEfficiency() { return efficiency; }
}