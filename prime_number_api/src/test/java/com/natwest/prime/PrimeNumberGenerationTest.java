package com.natwest.prime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class PrimeNumberGenerationTest {

    private PrimeNumberGeneration primeNumberGeneration;

    @BeforeEach
    void setUp() {
        // Initialize PrimeService before each test
        // Use a spy to verify method calls (e.g., sequentialSieve vs concurrentSegmentedSieve)
        primeNumberGeneration = spy(new PrimeNumberGeneration());
    }

    @Test
    void testSieveOfEratosthenesForZero() {
        // Test with n = 0, should return an empty list
        List<Integer> primes = primeNumberGeneration.sieveOfEratathenes(0);
        assertTrue(primes.isEmpty(), "Primes for 0 should be an empty list.");
        // Verify that neither sieve method was called for n < 2
        verify(primeNumberGeneration, times(0)).sequentialSieve(anyInt());
        // Note: Cannot directly verify concurrentSegmentedSieve as it's private.
        // We rely on the public method's behavior.
    }

    @Test
    void testSieveOfEratosthenesForOne() {
        // Test with n = 1, should return an empty list
        List<Integer> primes = primeNumberGeneration.sieveOfEratathenes(1);
        assertTrue(primes.isEmpty(), "Primes for 1 should be an empty list.");
        verify(primeNumberGeneration, times(0)).sequentialSieve(anyInt());
    }

    @Test
    void testSieveOfEratosthenesForTwo() {
        // Test with n = 2, should return [2]
        List<Integer> expectedPrimes = Collections.singletonList(2);
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(2);
        assertEquals(expectedPrimes, actualPrimes, "Primes for 2 should be [2].");
        verify(primeNumberGeneration).sequentialSieve(2); // Verify sequential sieve was called
    }

    @Test
    void testSieveOfEratosthenesForTen() {
        // Test with n = 10, should return [2, 3, 5, 7]
        List<Integer> expectedPrimes = Arrays.asList(2, 3, 5, 7);
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(10);
        assertEquals(expectedPrimes, actualPrimes, "Primes for 10 should be [2, 3, 5, 7].");
        verify(primeNumberGeneration).sequentialSieve(10); // Verify sequential sieve was called
    }

    @Test
    void testSieveOfEratosthenesForOneHundred() {
        // Test with n = 100, checking a few primes to ensure correctness
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(100);
        assertEquals(25, actualPrimes.size(), "There should be 25 primes up to 100.");
        assertTrue(actualPrimes.contains(97), "List should contain 97.");
        assertTrue(actualPrimes.contains(2), "List should contain 2.");
        assertTrue(actualPrimes.contains(53), "List should contain 53.");
        verify(primeNumberGeneration).sequentialSieve(100); // Verify sequential sieve was called
    }

    @Test
    void testSieveOfEratosthenesUsesSequentialForBelowThreshold() {
        // Test with a number just below the threshold (assuming threshold is 100_000)
        int testNumber = 99_999;
        primeNumberGeneration.sieveOfEratathenes(testNumber);
        // Verify that the sequentialSieve method was called
        verify(primeNumberGeneration).sequentialSieve(testNumber);
        // Verify that concurrentSegmentedSieve was NOT called (indirectly, by not mocking it)
    }

    @Test
    void testSieveOfEratosthenesUsesConcurrentForAboveThreshold() {
        // Test with a number above the threshold (assuming threshold is 100_000)
        int testNumber = 200_000;
        primeNumberGeneration.sieveOfEratathenes(testNumber);
        // Verify that sequentialSieve was called for the sqrt(n) part
        // and that the concurrent path was taken for the main calculation.
        // Since concurrentSegmentedSieve is private, we can't directly verify it.
        // We infer its use if sequentialSieve is called with sqrt(n) and the result is correct.
        int limit = (int) Math.sqrt(testNumber);
        verify(primeNumberGeneration).sequentialSieve(limit); // Called for pre-sieve
        // We can't directly verify the private concurrentSegmentedSieve,
        // but we can ensure the public method returns correct results for large N.
    }

    @Test
    void testSieveOfEratosthenesForTwoHundredThousand() {
        // Test for a moderately large number that should trigger the concurrent sieve path
        int n = 200_000;
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(n);

        // Known count of primes up to 200,000 is 17,984
        assertEquals(17984, actualPrimes.size(), "Incorrect number of primes up to " + n);

        // Verify some known primes are present
        assertTrue(actualPrimes.contains(2), "Primes list should contain 2.");
        assertTrue(actualPrimes.contains(199999), "Primes list should contain 199999."); // A prime near the end
        assertTrue(actualPrimes.contains(100003), "Primes list should contain 100003."); // A prime in the middle
        assertTrue(actualPrimes.contains(99989), "Primes list should contain 99989."); // A prime near threshold

        // Verify that the list is sorted
        List<Integer> sortedPrimes = actualPrimes.stream().sorted().collect(Collectors.toList());
        assertEquals(sortedPrimes, actualPrimes, "The list of primes should be sorted.");
    }

    @Test
    void testSieveOfEratosthenesForMaxAllowedNumber() {
        // Test for the maximum allowed number to ensure correctness with concurrent sieve
        int n = 1_000_000;
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(n);

        // Known count of primes up to 1,000,000 is 78,498
        assertEquals(78498, actualPrimes.size(), "Incorrect number of primes up to " + n);

        // Verify some known primes are present
        assertTrue(actualPrimes.contains(2), "Primes list should contain 2.");
        assertTrue(actualPrimes.contains(999983), "Primes list should contain 999983."); // A prime near the end
        assertTrue(actualPrimes.contains(500009), "Primes list should contain 500009."); // A prime in the middle

        // Verify that the list is sorted
        List<Integer> sortedPrimes = actualPrimes.stream().sorted().collect(Collectors.toList());
        assertEquals(sortedPrimes, actualPrimes, "The list of primes should be sorted.");
    }
}
