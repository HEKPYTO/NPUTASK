package task;

import status.Operation;

public class VectorTask extends ComputationTask {
    private int vectorSize;
    private Operation vectorOperation;
    private boolean isOptimized;

    public VectorTask(long taskId, int priority, int memorySize, int computeUnits,
                      int batchSize, int vectorSize, Operation operation) {
        super(taskId, priority, memorySize, computeUnits, batchSize);
        this.vectorSize = Math.max(1, vectorSize);
        this.vectorOperation = operation;
        this.isOptimized = false;
        calculateExecutionTime();
    }

    @Override
    protected void calculateExecutionTime() {
        super.calculateExecutionTime();

        if (vectorOperation == null) {
            return;
        }

        double sizeFactor = Math.log10(Math.max(2, vectorSize)) / Math.log10(2);
        double operationFactor = getOperationFactor();
        double optimizationFactor = isOptimized ? 0.7 : 1.0;

        this.executionTime = (long)(this.executionTime * sizeFactor * operationFactor * optimizationFactor);
    }

    private double getOperationFactor() {
        return switch (vectorOperation) {
            case ADD -> 1.0;
            case MUL -> 1.2;
            case REDUCE -> 1.5;
        };
    }

    public void optimize() {
        if (!isOptimized) {
            isOptimized = true;
            calculateExecutionTime();
        }
    }

    public int getVectorSize() { return vectorSize; }
    public void setVectorSize(int size) {
        this.vectorSize = Math.max(1, size);
        calculateExecutionTime();
    }

    public Operation getVectorOperation() { return vectorOperation; }
    public void setVectorOperation(Operation operation) {
        this.vectorOperation = operation;
        calculateExecutionTime();
    }

    public boolean isOptimized() {
        return isOptimized;
    }
}