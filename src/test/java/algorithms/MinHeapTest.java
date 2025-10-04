package algorithms;

import metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapTest {

    @Test void empty_extract_throws() {
        MinHeap<Integer, String> h = new MinHeap<>();
        assertThrows(NoSuchElementException.class, h::extractMin);
        assertThrows(NoSuchElementException.class, h::peekMin);
    }

    @Test void single_element() {
        MinHeap<Integer, String> h = new MinHeap<>();
        h.insert(5, "A");
        assertEquals("A", h.peekMin());
        var e = h.extractMin();
        assertEquals(5, e.getKey());
        assertTrue(h.isEmpty());
    }

    @Test void duplicates_and_order() {
        MinHeap<Integer, String> h = new MinHeap<>();
        h.insert(1, "x1");
        h.insert(1, "x2");
        h.insert(0, "x0");
        assertEquals(0, h.extractMin().getKey());
        assertEquals(1, h.extractMin().getKey());
        assertEquals(1, h.extractMin().getKey());
    }

    @Test void decrease_key_promotes() {
        MinHeap<Integer, String> h = new MinHeap<>();
        MinHeap.Handle hc = h.insert(10, "C");
        h.insert(5, "B");
        h.decreaseKey(hc, 1);
        assertEquals(1, h.extractMin().getKey());
    }

    @Test void decrease_key_invalid_increase_rejected() {
        MinHeap<Integer, String> h = new MinHeap<>();
        MinHeap.Handle hc = h.insert(3, "C");
        assertThrows(IllegalArgumentException.class, () -> h.decreaseKey(hc, 9));
    }

    @Test void handle_invalid_after_extract() {
        MinHeap<Integer, String> h = new MinHeap<>();
        MinHeap.Handle hc = h.insert(3, "C");
        h.extractMin();
        assertFalse(hc.isValid());
        assertThrows(IllegalStateException.class, () -> h.decreaseKey(hc, 1));
    }

    @Test void merge_ok() {
        MinHeap<Integer, String> h1 = new MinHeap<>();
        MinHeap<Integer, String> h2 = new MinHeap<>();
        h1.insert(4, "a"); h1.insert(2, "b");
        h2.insert(3, "c"); h2.insert(1, "d");
        MinHeap<Integer,String> m = MinHeap.merge(h1, h2);
        assertTrue(h1.isEmpty() && h2.isEmpty());
        assertEquals(1, m.extractMin().getKey());
        assertEquals(2, m.extractMin().getKey());
        assertEquals(3, m.extractMin().getKey());
        assertEquals(4, m.extractMin().getKey());
    }

    @Test void merge_comparator_mismatch() {
        MinHeap<Integer, String> h1 = new MinHeap<>(Comparator.naturalOrder());
        MinHeap<Integer, String> h2 = new MinHeap<>(Comparator.reverseOrder());
        h1.insert(1, "a"); h2.insert(2, "b");
        assertThrows(IllegalArgumentException.class, () -> MinHeap.merge(h1, h2));
    }

    @Test void metrics_counts_something() {
        PerformanceTracker p = new PerformanceTracker();
        MinHeap<Integer,String> h = new MinHeap<>(Comparator.naturalOrder(), p);
        h.insert(2, "a"); h.insert(1, "b");
        h.extractMin();
        assertTrue(p.getComparisons() > 0);
        assertTrue(p.getArrayGets() > 0);
        assertTrue(p.getAllocations() >= 2);
    }

    @Test void property_random_correctness() {
        Random rnd = new Random(0);
        for (int t = 0; t < 20; t++) {
            int n = 200;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1000); // duplicates allowed
            MinHeap<Integer,Integer> h = new MinHeap<>();
            for (int x : a) h.insert(x, x);
            int prev = Integer.MIN_VALUE;
            while (!h.isEmpty()) {
                int k = h.extractMin().getKey();
                assertTrue(k >= prev);
                prev = k;
            }
        }
    }
}