package org.example.jubjubapi.global.lock.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.global.lock.exception.LockAcquisitionFailedException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 분산락 유틸클래스입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

    private static final long WAIT_TIME_SECONDS = 5L; // 락 대기 시간
    private static final long LEASE_TIME_SECONDS = 3L; // 락 점유 시간

    private final RedissonClient redissonClient;

    public <T> T execute(String lockKey, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new LockAcquisitionFailedException();
            }
            return task.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionFailedException();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
