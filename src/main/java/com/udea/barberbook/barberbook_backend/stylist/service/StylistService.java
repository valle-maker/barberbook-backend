package com.udea.barberbook.barberbook_backend.stylist.service;

import java.util.List;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.stylist.dto.CreateStylistRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.StylistResponse;

public interface StylistService {

    StylistResponse createStylist(CreateStylistRequest request);

    StylistResponse assignServices(UUID stylistId, List<UUID> serviceIds);
}
