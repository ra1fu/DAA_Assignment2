package cli;

import algorithms.MinHeap;
import metrics.PerformanceTracker;

import java.io.*;
import java.util.*;

/**
 * Simple CLI to benchmark MinHeap operations and export CSV.
 * Example:
 *   java -jar min-heap-cli.jar --n 1000,10000 --trials 5 --dist random --csv results.csv
 */
public class BenchmarkRunner {
    record Config(int[] sizes, int trials, String dist, String csv) {}

    public static void main(String[] args) throws Exception {
        Config cfg = parseArgs(args);
        System.out.println("Min-Heap Benchmark");
        System.out.println("sizes=" + Arrays.toString(cfg.sizes()) + ", trials=" + cfg.trials() + ", dist=" + cfg.dist());
        try (PrintWriter out = cfg.csv() == null ? new PrintWriter(System.out) : new PrintWriter(new File(cfg.csv()))) {
            out.println("n,trial,time_ms,comparisons,swaps,arrayGets,arraySets,allocations");
            for (int n : cfg.sizes()) {
                for (int t = 1; t <= cfg.trials(); t++) {
                    PerformanceTracker perf = new PerformanceTracker();
                    MinHeap<Integer, Integer> heap = new MinHeap<>(Comparator.naturalOrder(), perf);
                    int[] data = generate(n, cfg.dist());
                    long start = System.nanoTime();
                    // workload: bulk inserts + random decreaseKey + extracts
                    MinHeap.Handle[] handles = new MinHeap.Handle[n];
                    for (int i = 0; i < n; i++) handles[i] = heap.insert(data[i], data[i]);
                    Random rnd = new Random(42L);
                    for (int i = 0; i < n/10; i++) {
                        int idx = rnd.nextInt(n);
                        if (handles[idx] != null && handles[idx].isValid()) {
                            int newKey = data[idx] - rnd.nextInt(10); // <= old likely
                            try { heap.decreaseKey(handles[idx], newKey); } catch (IllegalArgumentException ignored) {}
                        }
                    }
                    int sum = 0;
                    while (!heap.isEmpty()) sum += heap.extractMin().getKey();
                    long end = System.nanoTime();
                    long ms = (end - start) / 1_000_000;
                    out.printf(Locale.US, "%d,%d,%d,%d,%d,%d,%d,%d%n",
                            n, t, ms,
                            perf.getComparisons(), perf.getSwaps(), perf.getArrayGets(), perf.getArraySets(), perf.getAllocations());
                }
            }
        }
    }

    private static int[] generate(int n, String dist) {
        int[] a = new int[n];
        switch (dist.toLowerCase(Locale.ROOT)) {
            case "sorted" -> { for (int i = 0; i < n; i++) a[i] = i; }
            case "reversed" -> { for (int i = 0; i < n; i++) a[i] = n - i; }
            case "nearly" -> {
                for (int i = 0; i < n; i++) a[i] = i;
                Random rnd = new Random(123);
                for (int i = 0; i < n/100; i++) { // 1% noise
                    int i1 = rnd.nextInt(n), i2 = rnd.nextInt(n);
                    int tmp = a[i1]; a[i1] = a[i2]; a[i2] = tmp;
                }
            }
            default -> { // random
                Random rnd = new Random(1);
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt();
            }
        }
        return a;
    }

    private static Config parseArgs(String[] args) {
        int[] sizes = new int[]{100, 1000, 10000};
        int trials = 3; String dist = "random"; String csv = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--n" -> sizes = Arrays.stream(args[++i].split(",")).mapToInt(Integer::parseInt).toArray();
                case "--trials" -> trials = Integer.parseInt(args[++i]);
                case "--dist" -> dist = args[++i];
                case "--csv" -> csv = args[++i];
                default -> {}
            }
        }
        return new Config(sizes, trials, dist, csv);
    }
}