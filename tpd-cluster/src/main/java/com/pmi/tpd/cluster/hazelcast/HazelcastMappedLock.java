package com.pmi.tpd.cluster.hazelcast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.hazelcast.map.IMap;

class HazelcastMappedLock {

    private final ThreadLocal<Map<String, Integer>> acquiredLocks;

    private final IMap<String, String> lockMap;

    public HazelcastMappedLock(final IMap<String, String> lockMap) {
        this.acquiredLocks = new ThreadLocal<Map<String, Integer>>() {

            @Override
            protected Map<String, Integer> initialValue() {
                return new HashMap<String, Integer>();
            }
        };
        this.lockMap = lockMap;
    }

    public boolean isHeldByCurrentThread(final String lockName) {
        return ((Map<?, ?>) this.acquiredLocks.get()).containsKey(lockName);
    }

    public void lock(final String lockName) {
        this.lockMap.lock(lockName);
        onAcquired(lockName);
    }

    public void lockInterruptibly(final String lockName) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException("Thread has been interrupted before attempting to acquire lock " + lockName);

        }

        while (!this.lockMap.tryLock(lockName, 1L, TimeUnit.DAYS)) {

        }

        onAcquired(lockName);
    }

    public boolean tryLock(final String lockName) {
        if (this.lockMap.tryLock(lockName)) {
            onAcquired(lockName);
            return true;
        }
        return false;
    }

    public boolean tryLock(final String lockName, final long time, final TimeUnit unit) throws InterruptedException {
        if (this.lockMap.tryLock(lockName, time, unit)) {
            onAcquired(lockName);
            return true;
        }
        return false;
    }

    public void unlock(final String lockName) {
        this.lockMap.unlock(lockName);
        onRelease(lockName);
    }

    private void onAcquired(final String lockName) {
        final Map<String, Integer> map = this.acquiredLocks.get();
        final Integer count = map.get(lockName);
        map.put(lockName, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
    }

    private void onRelease(final String lockName) {
        final Map<String, Integer> map = this.acquiredLocks.get();
        final Integer count = map.get(lockName);
        if (count == null) {
            throw new IllegalStateException("Lock " + lockName + " wasn't held prior to unlock being called!");
        }
        if (count.intValue() == 1) {
            map.remove(lockName);
        } else {
            map.put(lockName, Integer.valueOf(count.intValue() - 1));
        }
    }
}
