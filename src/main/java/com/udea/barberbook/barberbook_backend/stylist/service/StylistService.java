package com.udea.barberbook.barberbook_backend.stylist.service;

import java.util.List;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.stylist.dto.CreateStylistRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.PublicStylistResponse;
import com.udea.barberbook.barberbook_backend.stylist.dto.StylistResponse;
import com.udea.barberbook.barberbook_backend.stylist.dto.UpdateStylistProfileRequest;

public interface StylistService {

    StylistResponse createStylist(CreateStylistRequest request);

    StylistResponse assignServices(UUID stylistId, List<UUID> serviceIds);

    StylistResponse updateProfile(UUID stylistId, UpdateStylistProfileRequest request);

    // Active stylists for the client app; serviceId null = all of them.
    List<PublicStylistResponse> listActive(UUID serviceId);
}
