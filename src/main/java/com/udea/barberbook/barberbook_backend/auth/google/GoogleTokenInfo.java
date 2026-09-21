package com.udea.barberbook.barberbook_backend.auth.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleTokenInfo(
    @JsonProperty("sub") String subject,
    @JsonProperty("email") String email,
    @JsonProperty("aud") String audience,
    @JsonProperty("name") String name
) {}
