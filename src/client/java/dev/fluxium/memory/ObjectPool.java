package dev.fluxium.memory;

import java.util.ArrayDeque;
import java.util.function.Supplier;

public final class ObjectPool<T> {
    private final ArrayDeque<T> pool = new ArrayDeque<>(32);
    private final Supplier<T> factory;
    private final int maxSize;

    public ObjectPool(Supplier<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    public T acquire() {
        T obj = pool.pollFirst();
        if (obj != null) {
            MemoryOptimizer.INSTANCE.recordCacheHit();
            MemoryOptimizer.INSTANCE.recordAllocationAvoided();
            return obj;
        }
        MemoryOptimizer.INSTANCE.recordCacheMiss();
        return factory.get();
    }

    public void release(T obj) {
        if (pool.size() < maxSize) {
            pool.offerLast(obj);
        }
    }

    public void clear() {
        pool.clear();
    }
}
