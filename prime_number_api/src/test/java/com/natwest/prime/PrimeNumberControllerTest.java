package com.natwest.prime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrimeNumberControllerTest.class)
public class PrimeNumberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrimeNumberGeneration primeNumberGeneration;

    @Test
    void testGetPrimesSuccessJson() throws Exception {
        List<Integer> expectedPrimes = Arrays.asList(2, 3, 5, 7);
        when(primeNumberGeneration.sieveOfEratathenes(10)).thenReturn(expectedPrimes);
        // For application/xml
        mockMvc.perform(get("/primes/10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect((ResultMatcher) content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(10))
                .andExpect(jsonPath("$.primes[0]").value(2))
                .andExpect(jsonPath("$.primes[3]").value(7))
                .andExpect(jsonPath("$.primes.length()").value(4));
    }

    @Test
    void testGetPrimesSuccessXml() throws Exception {
        List<Integer> expectedPrimes = Arrays.asList(2, 3, 5, 7);
        when(primeNumberGeneration.sieveOfEratathenes(10)).thenReturn(expectedPrimes);

        // For application/xml
        mockMvc.perform(get("/primes/10")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/HashMap/number").string("10"))
                .andExpect(xpath("/HashMap/primes/primes[1]").string("2"))
                .andExpect(xpath("/HashMap/primes/primes[4]").string("7"));
    }

    @Test
    void testGetPrimesNegativeNumber() throws Exception {
        // Perform GET request with a negative number
        mockMvc.perform(get("/primes/-5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Number must be a non-negative integer."));
    }

    @Test
    void testGetPrimesNumberTooLarge() throws Exception {
        when(primeNumberGeneration.sieveOfEratathenes(anyInt())).thenReturn(Collections.emptyList());
        // Perform GET request with a number exceeding the limit
        mockMvc.perform(get("/primes/10000001")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Number too large. Max allowed is 10,000,000 to prevent timeout."));
    }

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().string("Welcome to the Prime Number Service (Java)! To get prime numbers, use the endpoint /primes/<your_number>. For example, /primes/100"));
    }
}
