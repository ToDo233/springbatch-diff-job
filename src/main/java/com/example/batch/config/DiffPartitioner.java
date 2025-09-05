package com.example.batch.config;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom partitioner component that implements the Partitioner interface.
 * This class is responsible for creating execution contexts for parallel processing.
 */
@Component
public class DiffPartitioner implements Partitioner {
    /**
     * Partitions the workload into a specified number of grids.
     *
     * @param gridSize The number of partitions to create
     * @return A map where each key is a partition name and each value is an execution context
     *         containing partition information
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // Initialize a map to store partition names and their execution contexts
        Map<String, ExecutionContext> result = new HashMap<>();

        // Iterate through each partition index
        for (int i = 0; i < gridSize; i++) {
            // Create a new execution context for each partition
            ExecutionContext context = new ExecutionContext();
            // Set the current partition index and total partition count in the context
            context.putInt("partitionIndex", i);
            context.putInt("partitionCount", gridSize);
            // Add the partition to the result map with a unique key
            result.put("partition-" + i, context);
        }
        return result;
    }
}
