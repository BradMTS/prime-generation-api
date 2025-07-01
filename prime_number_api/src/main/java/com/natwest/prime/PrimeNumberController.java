package com.natwest.prime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/")
public class PrimeNumberController {

    private final PrimeNumberGeneration primeNumberGeneration;
    private static final Logger logger = LoggerFactory.getLogger(PrimeNumberController.class);

    @Autowired
    public PrimeNumberController(PrimeNumberGeneration primeNumberGeneration) {
        this.primeNumberGeneration = primeNumberGeneration;
        logger.info("PrimeNumberController initialized.");
    }

    /**
     * RESTful endpoint to calculate and return prime numbers up to a given 'number'.
     * Accessible via GET /primes/{number}.
     *
     * @param number The upper limit provided in the URL path.
     * @return ResponseEntity containing a JSON/XML response with primes or an error message.
     */
    @GetMapping(value = "/primes/{number}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Map<String, Object>> getPrimes(@PathVariable int number) {
        logger.info("Received request for primes up to: {}", number);
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = new HashMap<>();

        if (number < 0) {
            logger.warn("Invalid input: Number {} is negative.", number);
            response.put("error", "Number must be a non-negative integer.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (number > 10_000_000) {
            logger.warn("Input number {} exceeds maximum allowed limit.", number);
            response.put("error", "Number too large. Max allowed is 10,000,000 to prevent timeout.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<Integer> primes = primeNumberGeneration.sieveOfEratathenes(number);
        response.put("initial", number);
        response.put("primes", primes);
        logger.info("Successfully returned {} primes for number: {}", primes.size(), number);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("Successfully returned {} primes for initial: {}. Request completed in {} ms.", primes.size(), number, duration);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Accessible via GET /.
     * @return A welcome guidance as string.
     */
    @GetMapping("/")
    public String index() {
        return "Welcome to the Prime Number Generation Service! To get prime numbers below or equal to your number, use the endpoint /primes/<your_number>. For example, /primes/100";
    }
}
