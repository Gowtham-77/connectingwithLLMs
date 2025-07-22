package com.core.io.reddit_persona.controller;

import com.core.io.reddit_persona.dto.request.RedditUserRequest;
import com.core.io.reddit_persona.dto.response.PersonaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PersonaController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/persona")
    public ResponseEntity<?> generatePersona(@RequestBody RedditUserRequest request) throws IOException {
        String username = request.getUsername();

        String commentsUrl = "https://www.reddit.com/user/" + username + "/comments.json";
        String postsUrl = "https://www.reddit.com/user/" + username + "/submitted.json";

        String comments = restTemplate.getForObject(commentsUrl, String.class);
        String posts = restTemplate.getForObject(postsUrl, String.class);

        Files.writeString(Path.of("comments.json"), comments);
        Files.writeString(Path.of("posts.json"), posts);

        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Users\\ASUS\\OneDrive\\Desktop\\reddit-persona\\.venv\\Scripts\\python.exe",
                "src/main/java/com/core/io/reddit_persona/persona-generator.py",
                username
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String result = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        System.out.println("PYTHON OUTPUT:\n" + result);

        if (result == null || result.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Python script returned no output. Check for OpenAI/API errors.");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PersonaResponse persona = objectMapper.readValue(result, PersonaResponse.class);
            return ResponseEntity.ok(persona);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to parse Python output as JSON. Raw output: " + result);
        }
    }
}