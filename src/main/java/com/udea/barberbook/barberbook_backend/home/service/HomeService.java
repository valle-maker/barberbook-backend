package com.udea.barberbook.barberbook_backend.home.service;

import java.util.List;

import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;
import com.udea.barberbook.barberbook_backend.home.dto.RecommendationsResponse;

public interface HomeService {

    // Most booked active services (cancelled bookings don't count); limit is capped at 20.
    List<ServiceResponse> popularServices(int limit);

    RecommendationsResponse recommendations(String clientEmail);
}
