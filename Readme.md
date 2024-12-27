# NPU Task Management System

A comprehensive Neural Processing Unit (NPU) task management system for handling diverse computational needs in neural network operations.

## Overview
This system provides a framework for managing various neural processing tasks, from basic vector calculations to complex tensor operations, while optimizing resource utilization and maintaining accurate performance metrics.

## Features
- Task priority management (100-139)
- Memory allocation and bandwidth control
- Vector and tensor operation handling
- Multiple memory type support (Cache, RAM, VRAM, Disk)
- Synchronization modes for parallel processing
- Power consumption tracking
- Execution time calculations

## Class Structure

### Base Classes
- **NPUTask**: Base task unit with fundamental properties
- Task identification
- Priority management
- Resource tracking
- Execution timing
- Power consumption calculations

### Specialized Tasks
- **ComputationTask**: Resource allocation for computational workloads
- Processing unit management
- Batch processing capabilities
- Efficiency calculations

- **VectorTask**: Vector mathematics operations
- Addition, multiplication, reduction operations
- Performance optimization
- Size-based scaling

- **TensorTask**: Multi-dimensional data computations
- Dimension management
- Quantization support (INT8, FLOAT32, BFLOAT16)
- Sparsity handling

- **MemoryTask**: Memory transfer operations
- Bandwidth management
- Multiple memory type support
- Transfer time calculations

- **SyncTask**: Task synchronization
- Multiple sync modes (Barrier, Pipeline, Wavefront, Async)
- Frequency and buffer management
- Latency control

## Synchronization Modes
- **BARRIER**: Full synchronization point
- **PIPELINE**: Sequential task execution
- **WAVEFRONT**: Wave-pattern execution
- **ASYNC**: Independent execution

## Memory Types
- **CACHE**: High-speed temporary storage
- **RAM**: Main system memory
- **VRAM**: Graphics and neural processing memory
- **DISK**: Long-term storage system

## Vector Operations
- **ADD**: Vector addition
- **MUL**: Vector multiplication
- **REDUCE**: Vector reduction

## Requirements
- Java Development Kit (JDK)
- JUnit for testing

## Building the Project
1. Create a Java Project named "2110215_Midterm_Part2"
2. Copy provided folders into project's src folder
3. Implement required classes
4. Run JUnit tests from test package
5. Create UML diagram as UML.png
6. Export JAR file as {YourID}_Progmeth_Part2.jar

## File Structure
```
2110215_Midterm_Part2/
├── src/
│   ├── status/
│   │   ├── Memory.java
│   │   ├── Operation.java
│   │   ├── Quantization.java
│   │   ├── Status.java
│   │   └── Sync.java
│   └── task/
│       ├── NPUTask.java
│       ├── compute/
│       │   ├── ComputationTask.java
│       │   └── advanced/
│       │       ├── TensorTask.java
│       │       └── VectorTask.java
│       ├── memory/
│       │   └── MemoryTask.java
│       └── sync/
│           └── SyncTask.java
├── test/
│   └── [Test files]
├── UML.png
└── {YourID}_Progmeth_Part2.jar
```

## Deliverables
- Implemented Java classes
- UML class diagram (UML.png)
- JAR file with source code and compiled classes

Note: JAR file is mandatory for grading. Project will not be graded without proper JAR file submission.