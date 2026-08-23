package com.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SecureVaultApplication
 *
 * This is the entry point of the Spring Boot application.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration       : marks this class as a source of Spring beans
 *   - @EnableAutoConfiguration : tells Spring Boot to auto-configure beans
 *   - @ComponentScan       : scans this package and sub-packages for components
 */
@SpringBootApplication
public class SecureVaultApplication {

    private static final Logger logger = LoggerFactory.getLogger(SecureVaultApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SecureVaultApplication.class, args);
        logger.info("SecureVault application started successfully on http://localhost:8080");
        logger.info("API Documentation: Use proper API client or Postman for testing endpoints");
    }
}
