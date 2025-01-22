package task;

import status.Quantization;

public class TensorTask extends ComputationTask {
    private int[] dimensions;
    private Quantization tensorType;
    private final double sparsity;

    private static final double MAX_SPARSITY = 0.95;

    public TensorTask(long taskId, int priority, int memorySize, int computeUnits,
                      int batchSize, int[] dimensions, Quantization tensorType) {
        super(taskId, priority, memorySize, computeUnits, batchSize);
        this.dimensions = dimensions.clone();
        this.tensorType = tensorType;
        this.sparsity = calculateSparsity();
        calculateExecutionTime();
    }

    @Override
    protected void calculateExecutionTime() {
        super.calculateExecutionTime();

        if (tensorType == null || dimensions == null) {

            return;
        }

        super.calculateExecutionTime();
        double dimensionFactor = calculateDimensionFactor();
        double typeFactor = getTensorTypeFactor();
        double sparsityFactor = Math.max(0.1, 1.0 - (sparsity * 0.5));

        this.executionTime = (long)(super.getExecutionTime() * dimensionFactor * typeFactor * sparsityFactor);
    }

    private double calculateDimensionFactor() {
        if (dimensions == null || dimensions.length == 0) {
            return 1.0;
        }

        double logSum = 0.0;
        for (int dim : dimensions) {
            if (dim > 1) {
                logSum += Math.log10(dim);
            }
        }

        return (logSum + Math.log10(1 + Math.pow(10, logSum))) / Math.log10(2);
    }

    private double getTensorTypeFactor() {
        if (tensorType == null) return 1.0;
        return switch(tensorType) {
            case FLOAT32 -> 1.8;
            case INT8 -> 0.4;
            case BFLOAT16 -> 1.0;
        };
    }

    private double calculateSparsity() {
        return Math.min(Math.random(), MAX_SPARSITY);
    }

    public int[] getDimensions() {
        return dimensions.clone();
    }

    public void setDimensions(int[] dims) {
        this.dimensions = dims.clone();
        calculateExecutionTime();
    }

    public Quantization getTensorType() { return tensorType; }
    public void setTensorType(Quantization type) {
        this.tensorType = type;
        calculateExecutionTime();
    }

    public double getSparsity() {
        return sparsity;
    }
}