package com.udea.barberbook.barberbook_backend.user.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.dto.MeResponse;
import com.udea.barberbook.barberbook_backend.user.dto.UpdateProfileRequest;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MeResponse getProfile(String email) {
        return toResponse(find(email));
    }

    @Override
    @Transactional
    public MeResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = find(email);
        String photo = request.photoUrl();
        user.setPhotoUrl(photo == null || photo.isBlank() ? null : photo.trim());
        return toResponse(userRepository.save(user));
    }

    private User find(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }

    private MeResponse toResponse(User user) {
        String fullName = user.getFullName().trim();
        String firstName = fullName.split("\\s+")[0];
        return new MeResponse(
            user.getId(), fullName, firstName, user.getEmail(), user.getPhone(),
            user.getRole().toFrontendValue(), user.getPhotoUrl());
    }
}
