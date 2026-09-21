package com.udea.barberbook.barberbook_backend.auth.google;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.udea.barberbook.barberbook_backend.common.exception.InvalidGoogleTokenException;

// Verifies a Google Identity Services id_token via Google's public tokeninfo
// endpoint — no Client Secret needed for this call, only the Client ID to check
// the "aud" claim against once it's configured (see AuthServiceImpl).
@Component
public class GoogleTokenVerifier {

    private final RestClient restClient = RestClient.create("https://oauth2.googleapis.com");

    public GoogleTokenInfo verify(String idToken) {
        try {
            return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tokeninfo").queryParam("id_token", idToken).build())
                .retrieve()
                .body(GoogleTokenInfo.class);
        } catch (RestClientException ex) {
            throw new InvalidGoogleTokenException("Token de Google inválido o expirado.");
        }
    }
}
