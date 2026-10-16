package com.udea.barberbook.barberbook_backend.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// "Now" for business rules is the barbershop's local time, not the server's
// (Render runs in UTC).
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(@Value("${app.business.zone}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
