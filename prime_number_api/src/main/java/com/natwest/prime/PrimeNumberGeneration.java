package com.natwest.prime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Service
public class PrimeNumberGeneration {

    private static final Logger logger = LoggerFactory.getLogger(PrimeNumberGeneration.class);
    private static final int CONCURRENT_THRESHOLD = 100_000;

    private final ExecutorService executorService;

    public PrimeNumberGeneration() {
        int numCores = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(numCores);
        logger.info("PrimeService initialized with a thread pool of {} cores for concurrent sieve.", numCores);
    }

    /**
     * Calculates all prime numbers up to and including 'n' using the
     * Sieve of Eratosthenes algorithm
     * @param n The upper limit for prime number calculation.
     * @return A list of prime numbers up to 'n'.
     */
    @Cacheable("primes") //
    public List<Integer> sieveOfEratathenes(int n) {
        logger.info("Calculating primes for n={}. Checking cache first...", n);

        if (n < 2) {
            return new ArrayList<>();
        }

        if (n <= CONCURRENT_THRESHOLD) {
            logger.debug("Using sequential sieve for n={}", n);
            return sequentialSieve(n);
        } else {
            logger.debug("Using concurrent segmented sieve for n={}", n);
            return concurrentSegmentedSieve(n);
        }
    }

    /**
     * Standard sequential Sieve of Eratosthenes.
     */
    List<Integer> sequentialSieve(int n) {
        boolean[] prime = new boolean[n + 1];
        Arrays.fill(prime, true);

        for (int p = 2; p * p <= n; p++) {
            if (prime[p]) {
                for (int i = p * p; i <= n; i += p)
                    prime[i] = false;
            }
        }

        List<Integer> primes = new ArrayList<>();
        for (int p = 2; p <= n; p++) {
            if (prime[p]) {
                primes.add(p);
            }
        }
        return primes;
    }

    /**
     * Concurrent Segmented Sieve of Eratosthenes.
     */
    private List<Integer> concurrentSegmentedSieve(int n) {
        List<Integer> primes = new ArrayList<>();

        int limit = (int) Math.sqrt(n);
        List<Integer> smallPrimes = sequentialSieve(limit);
        primes.addAll(smallPrimes);

        int segmentSize = Math.max(100_000, limit);
        if (segmentSize > 10_000_000) segmentSize = 10_000_000;

        List<Future<List<Integer>>> futures = new ArrayList<>();
        for (long low = limit + 1; low <= n; low += segmentSize) {
            long high = Math.min(low + segmentSize - 1, n);

            Callable<List<Integer>> segmentTask = new SegmentSieveTask(low, high, smallPrimes);
            futures.add(executorService.submit(segmentTask));
        }

        for (Future<List<Integer>> future : futures) {
            try {
                primes.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error during concurrent segment sieve: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to compute primes concurrently", e);
            }
        }

        Collections.sort(primes);
        return primes;
    }


    private static class SegmentSieveTask implements Callable<List<Integer>> {
        private final long low;
        private final long high;
        private final List<Integer> smallPrimes;

        public SegmentSieveTask(long low, long high, List<Integer> smallPrimes) {
            this.low = low;
            this.high = high;
            this.smallPrimes = smallPrimes;
        }

        @Override
        public List<Integer> call() throws Exception {
            boolean[] isPrime = new boolean[(int) (high - low + 1)];
            Arrays.fill(isPrime, true);

            for (int p : smallPrimes) {
                long startMultiple = Math.max((long) p * p, (low + p - 1) / p * p);

                for (long i = startMultiple; i <= high; i += p) {
                    if (i >= low) {
                        isPrime[(int) (i - low)] = false;
                    }
                }
            }

            List<Integer> segmentPrimes = new ArrayList<>();

            for (long i = low; i <= high; i++) {
                if (isPrime[(int) (i - low)]) {
                    segmentPrimes.add((int) i);
                }
            }
            return segmentPrimes;
        }
    }
}
