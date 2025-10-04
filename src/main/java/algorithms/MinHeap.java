package algorithms;

import metrics.PerformanceTracker;

import java.util.*;

/**
 * Min-Heap with handles for O(log n) decreaseKey and O(n) merge via build-heap.
 * Supports custom Comparator. Tracks metrics via PerformanceTracker.
 */
public class MinHeap<K, V> {
    // ===== Public Handle API =====
    public static final class Handle {
        private int index = -1; // -1 => invalidated
        private Handle(int index) { this.index = index; }
        public boolean isValid() { return index >= 0; }
    }

    // ===== Internal Node =====
    private static final class Node<K, V> {
        K key; V value; Handle h;
        Node(K k, V v, Handle h) { this.key = k; this.value = v; this.h = h; }
    }

    // ===== Fields =====
    private final ArrayList<Node<K,V>> a = new ArrayList<>();
    private final Comparator<? super K> cmp; // may be null => Comparable
    private final PerformanceTracker perf;    // may be null => no tracking

    // ===== Constructors =====
    public MinHeap() { this(null, null); }
    public MinHeap(Comparator<? super K> cmp) { this(cmp, null); }
    public MinHeap(Comparator<? super K> cmp, PerformanceTracker perf) {
        this.cmp = cmp; this.perf = perf;
    }

    private MinHeap(ArrayList<Node<K,V>> items, Comparator<? super K> cmp, PerformanceTracker perf) {
        this.cmp = cmp; this.perf = perf;
        a.addAll(items);
        for (int i = 0; i < a.size(); i++) getNode(i).h.index = i;
        for (int i = parent(a.size()-1); i >= 0; i--) siftDown(i);
    }

    // ===== Public API =====
    public int size() { return a.size(); }
    public boolean isEmpty() { return a.isEmpty(); }

    /** Insert returns a Handle for O(log n) decreaseKey later. */
    public Handle insert(K key, V value) {
        checkKeyNotNull(key);
        Handle h = new Handle(a.size());
        Node<K,V> node = new Node<>(key, value, h);
        a.add(node); incAlloc();
        siftUp(h.index);
        return h;
    }

    public V peekMin() {
        ensureNotEmpty();
        return getNode(0).value;
    }

    /** Removes and returns the min (key, value) pair. */
    public Map.Entry<K,V> extractMin() {
        ensureNotEmpty();
        Node<K,V> root = getNode(0);
        swap(0, a.size()-1);
        Node<K,V> removed = a.remove(a.size()-1); setCount(); // shrink
        removed.h.index = -1; // invalidate handle
        if (!a.isEmpty()) siftDown(0);
        return Map.entry(root.key, root.value);
    }

    /** Returns current key for a handle. */
    public K keyOf(Handle h) { checkHandle(h); return getNode(h.index).key; }

    /** Decrease the key associated with a handle. New key must be <= old key. */
    public void decreaseKey(Handle h, K newKey) {
        checkHandle(h); checkKeyNotNull(newKey);
        Node<K,V> node = getNode(h.index);
        if (compare(newKey, node.key) > 0)
            throw new IllegalArgumentException("newKey greater than current key");
        node.key = newKey;
        siftUp(h.index);
    }

    /** Meld/merge two heaps into a new heap in O(n). Source heaps are cleared. */
    public static <K,V> MinHeap<K,V> merge(MinHeap<K,V> left, MinHeap<K,V> right) {
        if (left == null || right == null) throw new IllegalArgumentException("heaps must be non-null");
        if (!Objects.equals(left.cmp, right.cmp))
            throw new IllegalArgumentException("Comparators must match for merge");
        ArrayList<Node<K,V>> items = new ArrayList<>(left.a.size() + right.a.size());
        items.addAll(left.a); items.addAll(right.a);
        PerformanceTracker perf = left.perf != null ? left.perf : right.perf;
        left.a.clear(); right.a.clear();
        return new MinHeap<>(items, left.cmp, perf);
    }

    // ===== Helpers =====
    private static int parent(int i) { return (i - 1) / 2; }
    private static int left(int i)   { return 2*i + 1; }
    private static int right(int i)  { return 2*i + 2; }

    private int compare(K x, K y) {
        if (perf != null) perf.incComparison();
        if (cmp != null) return cmp.compare(x, y);
        @SuppressWarnings("unchecked") Comparable<? super K> cx = (Comparable<? super K>) x;
        return cx.compareTo(y);
    }

    private Node<K,V> getNode(int i) {
        if (perf != null) perf.incGet();
        return a.get(i);
    }
    private void setNode(int i, Node<K,V> n) {
        if (perf != null) perf.incSet();
        a.set(i, n);
    }
    private void setCount() { if (perf != null) perf.incSet(); }
    private void incAlloc() { if (perf != null) perf.incAllocation(); }

    private void siftUp(int i) {
        while (i > 0) {
            int p = parent(i);
            if (compare(getNode(i).key, getNode(p).key) < 0) {
                swap(i, p);
                i = p;
            } else break;
        }
    }

    private void siftDown(int i) {
        int n = a.size();
        while (true) {
            int l = left(i), r = right(i), m = i;
            if (l < n && compare(getNode(l).key, getNode(m).key) < 0) m = l;
            if (r < n && compare(getNode(r).key, getNode(m).key) < 0) m = r;
            if (m != i) { swap(i, m); i = m; } else break;
        }
    }

    private void swap(int i, int j) {
        if (i == j) return;
        if (perf != null) perf.incSwap();
        Node<K,V> ni = getNode(i), nj = getNode(j);
        setNode(i, nj); setNode(j, ni);
        nj.h.index = i; ni.h.index = j;
    }

    private void ensureNotEmpty() {
        if (a.isEmpty()) throw new NoSuchElementException("heap is empty");
    }
    private void checkHandle(Handle h) {
        if (h == null || !h.isValid() || h.index >= a.size() || getNode(h.index).h != h)
            throw new IllegalStateException("Invalid handle");
    }
    private void checkKeyNotNull(K key) {
        if (key == null) throw new NullPointerException("key must be non-null");
    }
}