package com.example.hiring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookRunner {
    private static final Logger log = LoggerFactory.getLogger(WebhookRunner.class);

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Application started - executing webhook flow");

        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> req = new HashMap<>();
        req.put("name", "John Doe");
        req.put("regNo", "REG12347"); // odd reg no - selects Question 1
        req.put("email", "john@example.com");

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(generateUrl, req, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map body = resp.getBody();
                String webhook = (String) body.get("webhook");
                String accessToken = (String) body.get("accessToken");
                log.info("Received webhook: {}", webhook);
                log.info("Received accessToken: {}", accessToken != null ? "[REDACTED]" : null);

                // Prepare final SQL query (Question 1 - highest salary not on 1st day)
                String finalQuery = "SELECT p.amount AS SALARY, CONCAT(e.first_name, ' ', e.last_name) AS NAME, "
                        + "TIMESTAMPDIFF(YEAR, e.dob, CURDATE()) AS AGE, d.department_name AS DEPARTMENT_NAME "
                        + "FROM payments p "
                        + "JOIN employee e ON p.emp_id = e.emp_id "
                        + "JOIN department d ON e.department = d.department_id "
                        + "WHERE DAY(p.payment_time) != 1 "
                        + "AND p.amount = (SELECT MAX(amount) FROM payments WHERE DAY(payment_time) != 1);";

                // Store final query locally
                storeFinalQuery(finalQuery);

                // Send the solution to the returned webhook URL
                if (webhook != null && accessToken != null) {
                    sendSolutionToWebhook(webhook, accessToken, finalQuery);
                } else {
                    log.error("webhook or accessToken missing in response body");
                }
            } else {
                log.error("Failed to generate webhook - status: {}", resp.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            log.error("HTTP error during generateWebhook: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Unexpected error during webhook flow", ex);
        }
    }

    private void sendSolutionToWebhook(String webhook, String accessToken, String finalQuery) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Use the token exactly as received per instructions (no Bearer prefix unless required)
            headers.set("Authorization", accessToken);

            Map<String, String> payload = new HashMap<>();
            payload.put("finalQuery", finalQuery);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
            log.info("Posting solution to webhook URL: {}", webhook);
            ResponseEntity<String> resp = restTemplate.postForEntity(webhook, entity, String.class);
            log.info("Webhook POST responded with status: {}", resp.getStatusCode());
        } catch (HttpClientErrorException ex) {
            log.error("HTTP error when posting solution: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Unexpected error when posting solution", ex);
        }
    }

    private void storeFinalQuery(String finalQuery) {
        try {
            Path dir = Paths.get("solutions");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            File out = dir.resolve("final_query.sql").toFile();
            try (FileWriter fw = new FileWriter(out)) {
                fw.write(finalQuery);
            }
            log.info("Final query written to {}", out.getAbsolutePath());
        } catch (Exception ex) {
            log.error("Failed to write final query to file", ex);
        }
    }
}
