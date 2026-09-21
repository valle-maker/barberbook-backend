package com.udea.barberbook.barberbook_backend.stylist.service;

import com.udea.barberbook.barberbook_backend.stylist.dto.CreateStylistRequest;
import com.udea.barberbook.barberbook_backend.stylist.dto.StylistResponse;

public interface StylistService {

    StylistResponse createStylist(CreateStylistRequest request);
}
