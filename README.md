# Min-Heap (decrease-key + merge)


**Student A (Pair 4)** — Java implementation of a binary **Min-Heap** with:
- `insert`, `peekMin`, `extractMin`
- **`decreaseKey(handle, newKey)`** in `O(log n)` via stable handles
- **`merge(left, right)`** in `O(n)` using build-heap
- Metrics: comparisons, swaps, array gets/sets, allocations
- CLI benchmark with CSV export


## Build & Test
```bash
mvn -q -DskipTests=false test
