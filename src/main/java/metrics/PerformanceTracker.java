package metrics;


/**
 * Tracks low-level operations for empirical complexity: comparisons, swaps,
 * array gets/sets, and approximate allocations (node creations).
 */
public class PerformanceTracker {
    private long comparisons;
    private long swaps;
    private long arrayGets;
    private long arraySets;
    private long allocations;


    public void incComparison() { comparisons++; }
    public void incSwap() { swaps++; }
    public void incGet() { arrayGets++; }
    public void incSet() { arraySets++; }
    public void incAllocation() { allocations++; }


    public long getComparisons() { return comparisons; }
    public long getSwaps() { return swaps; }
    public long getArrayGets() { return arrayGets; }
    public long getArraySets() { return arraySets; }
    public long getAllocations() { return allocations; }


    public void reset() {
        comparisons = swaps = arrayGets = arraySets = allocations = 0L;
    }


    @Override public String toString() {
        return "comparisons=" + comparisons +
                ", swaps=" + swaps +
                ", arrayGets=" + arrayGets +
                ", arraySets=" + arraySets +
                ", allocations=" + allocations;
    }
}