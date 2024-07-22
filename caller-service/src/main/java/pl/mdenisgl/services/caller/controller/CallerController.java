package pl.mdenisgl.services.caller.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@RequestMapping("/caller")
public class CallerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallerController.class);

    @Autowired
    Optional<BuildProperties> buildProperties;
    @Autowired
    RestTemplate restTemplate;
    @Value("${VERSION}")
    private String version;

    @GetMapping("/ping")
    public String ping() {
        LOGGER.info("Ping: name={}, version={}", buildProperties.or(Optional::empty), version);
        String response = restTemplate.getForObject("http://callme-service:8080/callme/ping", String.class);
        LOGGER.info("Calling: response={}", response);
        return "I'm caller-service " + version + ". Calling... " + response;
    }

    @GetMapping("/ping-with-random-error")
    public ResponseEntity<String> pingWithRandomError() {
        LOGGER.info("Ping with random error: name={}, version={}", buildProperties.or(Optional::empty), version);

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.getForEntity("http://callme-service:8080/callme/ping-with-random-error", String.class);

            LOGGER.info("Calling: responseCode={}, response={}", responseEntity.getStatusCode(), responseEntity.getBody());

            return new ResponseEntity<>("I'm caller-service " + version + ". Calling... " +
                    responseEntity.getBody(), responseEntity.getStatusCode());

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Handle HTTP 5xx/4xx errors returned by callme-service
            LOGGER.error("HTTP error occurred calling callme-service: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return new ResponseEntity<>("Error calling callme-service: " + ex.getStatusCode(), ex.getStatusCode());

        } catch (Exception ex) {
            // Handle any other unexpected exceptions
            LOGGER.error("Unexpected error occurred", ex);
            return new ResponseEntity<>("Unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/ping-with-random-delay")
    public String pingWithRandomDelay() {
        LOGGER.info("Ping with random delay: name={}, version={}", buildProperties.or(Optional::empty), version);
        String response = restTemplate
                .getForObject("http://callme-service:8080/callme/ping-with-random-delay", String.class);
        LOGGER.info("Calling: response={}", response);
        return "I'm caller-service " + version + ". Calling... " + response;
    }

}
