package com.udea.barberbook.barberbook_backend.stylist.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// What the client app may see of a stylist: no email or phone.
public record PublicStylistResponse(
    UUID id,
    String fullName,
    String specialty,
    String photoUrl,
    BigDecimal rating,
    List<UUID> serviceIds
) {}
