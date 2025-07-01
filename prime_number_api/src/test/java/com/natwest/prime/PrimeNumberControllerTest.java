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
    private MockMvc mockMvc; // Used to simulate HTTP requests

    @MockBean // Mocks the PrimeService dependency within the controller
    private PrimeNumberGeneration primeNumberGeneration;

    @Test
    void testGetPrimesSuccessJson() throws Exception {
        // Mock the service call to return a predefined list of primes
        List<Integer> expectedPrimes = Arrays.asList(2, 3, 5, 7);
        when(primeNumberGeneration.sieveOfEratathenes(10)).thenReturn(expectedPrimes);

        // Perform GET request to /primes/10 with Accept: application/json
        mockMvc.perform(get("/primes/10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect((ResultMatcher) content().contentType(MediaType.APPLICATION_JSON)) // Expect JSON content type
                .andExpect(jsonPath("$.number").value(10)) // Verify 'number' field in JSON
                .andExpect(jsonPath("$.primes[0]").value(2)) // Verify first prime
                .andExpect(jsonPath("$.primes[3]").value(7)) // Verify last prime
                .andExpect(jsonPath("$.primes.length()").value(4)); // Verify list size
    }

    @Test
    void testGetPrimesSuccessXml() throws Exception {
        // Mock the service call to return a predefined list of primes
        List<Integer> expectedPrimes = Arrays.asList(2, 3, 5, 7);
        when(primeNumberGeneration.sieveOfEratathenes(10)).thenReturn(expectedPrimes);

        // Perform GET request to /primes/10 with Accept: application/xml
        mockMvc.perform(get("/primes/10")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect((ResultMatcher) content().contentType(MediaType.APPLICATION_XML)) // Expect XML content type
                .andExpect(xpath("/HashMap/number").string("10")) // Verify 'number' field in XML
                .andExpect(xpath("/HashMap/primes/primes[1]").string("2")) // Verify first prime (XPath is 1-indexed)
                .andExpect(xpath("/HashMap/primes/primes[4]").string("7")); // Verify last prime
    }

    @Test
    void testGetPrimesNegativeNumber() throws Exception {
        // Perform GET request with a negative number
        mockMvc.perform(get("/primes/-5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(jsonPath("$.error").value("Number must be a non-negative integer.")); // Verify error message
    }

    @Test
    void testGetPrimesNumberTooLarge() throws Exception {
        // Mock the service call to ensure it's not called if validation fails
        when(primeNumberGeneration.sieveOfEratathenes(anyInt())).thenReturn(Collections.emptyList());

        // Perform GET request with a number exceeding the limit
        mockMvc.perform(get("/primes/1000001")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(jsonPath("$.error").value("Number too large. Max allowed is 1,000,000 to prevent timeout.")); // Verify error message
    }

    @Test
    void testIndexPage() throws Exception {
        // Test the root endpoint
        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect((ResultMatcher) content().string("Welcome to the Prime Number Service (Java)! To get prime numbers, use the endpoint /primes/<your_number>. For example, /primes/20")); // Verify content
    }
}
