package com.natwest.prime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PrimeNumberApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrimeNumberApplication.class, args);
    }
}
//curl -H "Accept: application/xml" http://localhost:8080/primes/20
