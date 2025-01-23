# NPU Task Management System

A comprehensive Neural Processing Unit (NPU) task management system for handling diverse computational needs in neural network operations.

## Overview
This system provides a framework for managing various neural processing tasks, from basic vector calculations to complex tensor operations, while optimizing resource utilization and maintaining accurate performance metrics.

## Features
- Task priority management (100-139, with 0-100 reserved for internal computer systems)
- Memory allocation and bandwidth control
- Vector and tensor operation handling
- Multiple memory type support (Cache, RAM, VRAM, Disk)
- Execution time calculations
- Power consumption tracking

## Class Structure

### Base Class
#### **NPUTask**: Base task unit with fundamental properties
- Task identification
- Priority management (100-139)
- Resource tracking
- Execution timing
- Power consumption calculations

### Specialized Tasks
#### **ComputeTask**: Resource allocation for computational workloads
- Processing unit management
- Batch processing capabilities (even numbers only)
- Efficiency calculations

#### **VectorTask**: Vector mathematics operations
- Addition, multiplication, reduction operations
- Performance optimization (30% reduction)
- Size-based logarithmic scaling
- Optimizable execution time

#### **TensorTask**: Multi-dimensional data computations
- Dimension management with immutability
- Quantization support (INT8, FLOAT32, BFLOAT16)
- Sparsity handling (0.0-0.95)
- Dimension scaling calculations

#### **MemoryTask**: Memory transfer operations
- Bandwidth management (minimum 1)
- Memory type multipliers
- Transfer time calculations

## Memory Types
- **CACHE**: High-speed temporary storage (0.5x)
- **RAM**: Main system memory (1.0x)
- **VRAM**: Graphics and neural processing memory (1.5x)
- **DISK**: Long-term storage system (5.0x)

## Vector Operations
- **ADD**: Vector addition (1.0x)
- **MUL**: Vector multiplication (1.2x)
- **REDUCE**: Vector reduction (1.5x)

## Quantization Formats
- **INT8**: 8-bit integer format (0.4x)
- **FLOAT32**: 32-bit floating-point format (1.8x)
- **BFLOAT16**: 16-bit brain floating-point format (1.0x)

## Requirements
- Java Development Kit (JDK)
- JUnit for testing

## Building the Project
1. Create a Java Project named "2110215_Midterm_Part2"
2. Copy provided folders into project's src folder
3. Implement required classes in package task
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
│   │   └── Status.java
│   └── task/
│       ├── NPUTask.java
│       ├── ComputeTask.java
│       ├── TensorTask.java
│       ├── VectorTask.java
│       └── MemoryTask.java
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