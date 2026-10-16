package com.udea.barberbook.barberbook_backend.user.service;

import com.udea.barberbook.barberbook_backend.user.dto.MeResponse;
import com.udea.barberbook.barberbook_backend.user.dto.UpdateProfileRequest;

public interface UserService {

    MeResponse getProfile(String email);

    MeResponse updateProfile(String email, UpdateProfileRequest request);
}
