package com.udea.barberbook.barberbook_backend.notification.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.barberbook.barberbook_backend.common.exception.NotificationNotFoundException;
import com.udea.barberbook.barberbook_backend.notification.domain.Notification;
import com.udea.barberbook.barberbook_backend.notification.domain.NotificationType;
import com.udea.barberbook.barberbook_backend.notification.dto.NotificationListResponse;
import com.udea.barberbook.barberbook_backend.notification.dto.NotificationResponse;
import com.udea.barberbook.barberbook_backend.notification.repository.NotificationRepository;
import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void create(User user, NotificationType type, String title, String message) {
        notificationRepository.save(Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .createdAt(Instant.now(clock))
            .build());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse list(String email, int limit) {
        User user = findUser(email);
        int size = Math.max(1, Math.min(limit, 50));
        List<NotificationResponse> items = notificationRepository
            .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, size)).stream()
            .map(n -> new NotificationResponse(
                n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getCreatedAt(), n.getReadAt() != null))
            .toList();
        return new NotificationListResponse(notificationRepository.countByUserIdAndReadAtIsNull(user.getId()), items);
    }

    @Override
    @Transactional
    public void markRead(String email, UUID notificationId) {
        User user = findUser(email);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
            .orElseThrow(() -> new NotificationNotFoundException("No se encontró la notificación solicitada."));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now(clock));
        }
    }

    @Override
    @Transactional
    public void markAllRead(String email) {
        notificationRepository.markAllRead(findUser(email).getId(), Instant.now(clock));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }
}
