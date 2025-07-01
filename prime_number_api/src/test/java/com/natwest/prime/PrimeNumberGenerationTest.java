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
        primeNumberGeneration = spy(new PrimeNumberGeneration());
    }

    @Test
    void testSieveOfEratosthenesForZero() {
        List<Integer> primes = primeNumberGeneration.sieveOfEratathenes(0);
        assertTrue(primes.isEmpty(), "Primes for 0 should be an empty list.");

        verify(primeNumberGeneration, times(0)).sequentialSieve(anyInt());
    }

    @Test
    void testSieveOfEratosthenesForOne() {
        List<Integer> primes = primeNumberGeneration.sieveOfEratathenes(1);
        assertTrue(primes.isEmpty(), "Primes for 1 should be an empty list.");
        verify(primeNumberGeneration, times(0)).sequentialSieve(anyInt());
    }

    @Test
    void testSieveOfEratosthenesForTwo() {
        List<Integer> expectedPrimes = Collections.singletonList(2);
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(2);
        assertEquals(expectedPrimes, actualPrimes, "Primes for 2 should be [2].");
        verify(primeNumberGeneration).sequentialSieve(2);
    }

    @Test
    void testSieveOfEratosthenesForTen() {
        List<Integer> expectedPrimes = Arrays.asList(2, 3, 5, 7);
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(10);
        assertEquals(expectedPrimes, actualPrimes, "Primes for 10 should be [2, 3, 5, 7].");
        verify(primeNumberGeneration).sequentialSieve(10);
    }

    @Test
    void testSieveOfEratosthenesForOneHundred() {
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(100);
        assertEquals(25, actualPrimes.size(), "There should be 25 primes up to 100.");
        assertTrue(actualPrimes.contains(97), "List should contain 97.");
        assertTrue(actualPrimes.contains(2), "List should contain 2.");
        assertTrue(actualPrimes.contains(53), "List should contain 53.");
        verify(primeNumberGeneration).sequentialSieve(100);
    }

    @Test
    void testSieveOfEratosthenesUsesSequentialForBelowThreshold() {
        int testNumber = 99_999;
        primeNumberGeneration.sieveOfEratathenes(testNumber);
        verify(primeNumberGeneration).sequentialSieve(testNumber);
    }

    @Test
    void testSieveOfEratosthenesUsesConcurrentForAboveThreshold() {
        int testNumber = 200_000;
        primeNumberGeneration.sieveOfEratathenes(testNumber);
        int limit = (int) Math.sqrt(testNumber);
        
        verify(primeNumberGeneration).sequentialSieve(limit); // Called for pre-sieve
    }

    @Test
    void testSieveOfEratosthenesForTwoHundredThousand() {
        // Test for a moderately large number that should trigger the concurrent sieve path
        int n = 200_000;
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(n);

        assertEquals(17984, actualPrimes.size(), "Incorrect number of primes up to " + n);

        assertTrue(actualPrimes.contains(2), "Primes list should contain 2.");
        assertTrue(actualPrimes.contains(199999), "Primes list should contain 199999.");
        assertTrue(actualPrimes.contains(100003), "Primes list should contain 100003.");
        assertTrue(actualPrimes.contains(99989), "Primes list should contain 99989.");

        // Verify the list sorting
        List<Integer> sortedPrimes = actualPrimes.stream().sorted().collect(Collectors.toList());
        assertEquals(sortedPrimes, actualPrimes, "The list of primes should be sorted.");
    }

    @Test
    void testSieveOfEratosthenesForMaxAllowedNumber() {
        int n = 10_000_000;
        List<Integer> actualPrimes = primeNumberGeneration.sieveOfEratathenes(n);

        assertEquals(664579, actualPrimes.size(), "Incorrect number of primes up to " + n);

        assertTrue(actualPrimes.contains(2), "Primes list should contain 2.");
        assertTrue(actualPrimes.contains(999983), "Primes list should contain 999983."); // A prime near the end
        assertTrue(actualPrimes.contains(500009), "Primes list should contain 500009."); // A prime in the middle

        List<Integer> sortedPrimes = actualPrimes.stream().sorted().collect(Collectors.toList());
        assertEquals(sortedPrimes, actualPrimes, "The list of primes should be sorted.");
    }
}
