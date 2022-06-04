package com.github.mcfongtw.cache;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;

@Slf4j
public class GuavaCacheTest {

    @Test
    public void testMockCacheLoaderWithExpireAfterAccess() throws Exception {
        final String VALUE = "VALUE";
        final String KEY = "KEY";
        final long REFRESH_INTERVAL = 1;

        FakeTicker fakeTicker = new FakeTicker();


        CacheLoader<String, String> mockCacheLoader = Mockito.mock(CacheLoader.class);

        when(mockCacheLoader.load(KEY)).thenReturn(VALUE);

        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .ticker(fakeTicker)
                .expireAfterAccess(REFRESH_INTERVAL, TimeUnit.SECONDS)
                .recordStats()
                .build(mockCacheLoader);

        Assertions.assertEquals(VALUE, cache.getUnchecked(KEY));
        verify(mockCacheLoader).load(KEY);

        Assertions.assertEquals(VALUE, cache.get(KEY));
        verifyZeroInteractions(mockCacheLoader);
        fakeTicker.advance(500, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(VALUE, cache.get(KEY));
        verifyZeroInteractions(mockCacheLoader);
        fakeTicker.advance(4, TimeUnit.SECONDS);
        Assertions.assertEquals(VALUE, cache.get(KEY));
        verify(mockCacheLoader, times(2)).load(KEY);
        Assertions.assertEquals(VALUE, cache.get(KEY));
        verifyZeroInteractions(mockCacheLoader);
    }

    @Test
    public void testCacheLoaderWithExpireAfterAccess() throws InterruptedException, ExecutionException {
        final String VALUE = "VALUE";
        final String KEY = "KEY";
        final long LOAD_WAIT_TIME = 1000L;
        final long REFRESH_INTERVAL = 1000L;

        final LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterAccess(REFRESH_INTERVAL, TimeUnit.MILLISECONDS)
                .recordStats()
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        log.info("load(), heavy work starts ...");
                        Thread.sleep(LOAD_WAIT_TIME);
                        log.info("load(), heavy work done ...");
                        return VALUE;
                    }
                });


        for (int i = 0; i < 10; i++) {
            if( i % 2 == 0) {
                Thread.sleep(REFRESH_INTERVAL / 2);
                cache.get(KEY);
                log.info("Round [{}] : [{}]", i, cache.stats());
                Assertions.assertEquals(i / 2, cache.stats().evictionCount());
                Assertions.assertEquals(i / 2, cache.stats().hitCount());
                Assertions.assertEquals((i + 1) / 2 + 1, cache.stats().loadSuccessCount());

            } else {
                Thread.sleep(REFRESH_INTERVAL + 500);
                Assertions.assertNull(cache.getIfPresent(KEY));
                cache.get(KEY);
                log.info("Round [{}] : [{}]", i, cache.stats());
                Assertions.assertEquals(i / 2 + 1, cache.stats().evictionCount());
                Assertions.assertEquals(i / 2, cache.stats().hitCount());
                Assertions.assertEquals((i + 1) / 2 + 1, cache.stats().loadSuccessCount());
            }
        }
    }

    @Test
    public void testCacheLoaderWithExpireAfterWrite() throws InterruptedException {
        final String VALUE = "VALUE";
        final String KEY = "KEY";
        final long LOAD_WAIT_TIME = 1000L;
        final long REFRESH_INTERVAL = 1000L;

        final LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(REFRESH_INTERVAL, TimeUnit.MILLISECONDS)
                .recordStats()
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        log.info("load(), heavy work starts ...");
                        Thread.sleep(LOAD_WAIT_TIME);
                        log.info("load(), heavy work done ...");
                        return VALUE;
                    }
                });


        for (int i = 0; i < 10; i++) {
            if( i % 2 == 0) {
                Thread.sleep(REFRESH_INTERVAL / 2);
                cache.put(KEY, VALUE);
                log.info("Round [{}] : [{}]", i, cache.stats());
                Assertions.assertEquals(i / 2, cache.stats().evictionCount());
                Assertions.assertEquals(0L, cache.stats().hitCount());

            } else {
                Thread.sleep(REFRESH_INTERVAL + 500);
                Assertions.assertNull(cache.getIfPresent(KEY));
                cache.put(KEY, VALUE);
                log.info("Round [{}] : [{}]", i, cache.stats());
                Assertions.assertEquals(i / 2 + 1, cache.stats().evictionCount());
                Assertions.assertEquals(0L, cache.stats().hitCount());
            }
        }
    }


    @Test
    public void testCacheLoaderWithAsyncRefreshAfterWrite() {
        final String OLD_VALUE = "OLD_VALUE";
        final String NEW_VALUE = "NEW_VALUE";
        final String KEY = "KEY";
        final long LOAD_WAIT_TIME = 1000L;
        final long REFRESH_INTERVAL = 10;

        FakeTicker fakeTicker = new FakeTicker();

        final AtomicBoolean firstCall = new AtomicBoolean(true);
        final LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(30)
                .ticker(fakeTicker)
                .refreshAfterWrite(REFRESH_INTERVAL, TimeUnit.SECONDS)
                .build(new AsyncCacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        log.info("load(), starts ...");
                        if (firstCall.get()) {
                            firstCall.set(false);
                            log.info("load(), first invocation, return quickly.");
                            return OLD_VALUE;
                        }

                        log.info("load(), heavy work starts ...");
                        Thread.sleep(LOAD_WAIT_TIME);
                        log.info("load(), heavy work done ...");
                        return NEW_VALUE;
                    }
                });


        String firstValue = cache.getUnchecked(KEY);
        Assertions.assertEquals(OLD_VALUE, firstValue);


        fakeTicker.advance(REFRESH_INTERVAL + 10, TimeUnit.SECONDS);

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            Future<String> client1 = executorService.submit(new Callable<String>() {
                public String call() {
                    log.info("client 1 is checking the cache");
                    return cache.getUnchecked(KEY);
                }
            });


            ExecutorService executorService2 = Executors.newCachedThreadPool();
            Future<String> client2 = executorService2.submit(new Callable<String>() {
                public String call() {
                    log.info("client 2 is checking the cache");
                    return cache.getUnchecked(KEY);
                }
            });

            String client1Value = client1.get();
            String client2Value = client2.get();
            // both client 1 and client 2 will still get old value
            // since new value is under async-loading
            Assertions.assertEquals(OLD_VALUE, client1Value);
            Assertions.assertEquals(OLD_VALUE, client2Value);

            Thread.sleep(LOAD_WAIT_TIME + 500);
            // now new value should be ready
            Assertions.assertEquals(NEW_VALUE, cache.getUnchecked(KEY));
        } catch (InterruptedException e) {
            Assertions.fail();
        } catch (ExecutionException e) {
            Assertions.fail();
        }
    }
}

@Slf4j
abstract class AsyncCacheLoader<K, V> extends CacheLoader<K, V> {

    private final ListeningExecutorService executorService =
            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    /**
     * The default ExecutorService is single-threaded.
     * Override this method to provide a meaningful multiple-thread implementation.
     */
    protected ListeningExecutorService executorService() {
        return executorService;
    }

    @Override
    public ListenableFuture<V> reload(final K key, final V oldValue) throws Exception {
        log.info("reload {}, try to call load() async-ly", key);
        // we need to load new values asynchronously, so that calls to read values from the cache don't block
        ListenableFuture<V> listenableFuture = executorService().submit(new Callable<V>() {
            @Override
            public V call() throws Exception {
                try {
                    V newValue = load(key);
                    log.info("reload {}, async-ly load() done. newValue = [{}]", key, newValue);
                    return newValue;
                } catch (Exception ex) {
                    log.error("Exception happens when reload " + key + ", return old value.", ex);
                    return oldValue;
                }
            }
        });
        return listenableFuture;
    }
}

class FakeTicker extends Ticker {
    private final AtomicLong nanos = new AtomicLong();

    public void advance(long time, TimeUnit timeUnit) {
        nanos.addAndGet(timeUnit.toNanos(time));
    }

    @Override
    public long read() {
        return nanos.get();
    }
}
