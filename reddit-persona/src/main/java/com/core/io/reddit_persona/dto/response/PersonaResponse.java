package com.core.io.reddit_persona.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PersonaResponse {
    private String name;
    private Integer age;
    private String location;
    private String status;
    private String occupation;
    private String archetype;
    private List<String> traits;

    private Map<String, Integer> motivations;
    private Map<String, Integer> personality;

    private List<String> behaviours;
    private List<String> frustrations;
    private List<String> goals;
    private String quote;
}