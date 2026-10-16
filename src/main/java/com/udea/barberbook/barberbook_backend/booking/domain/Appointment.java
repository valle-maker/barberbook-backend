package com.udea.barberbook.barberbook_backend.booking.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;
import com.udea.barberbook.barberbook_backend.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A booked slot. Availability (HU-12) subtracts CONFIRMADA appointments from a stylist's
// day; HU-13 creates them while holding a row lock on the stylist so two clients can
// never take overlapping time. Times are local to the business time zone (app.business.zone).
// client, code, price and createdAt are nullable at DB level only so rows created before
// HU-13 (test data) survive ddl-auto=update; the booking flow always fills them.
@Entity
@Table(name = "appointments", indexes = @Index(columnList = "stylist_id, start_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stylist_id", nullable = false)
    private Stylist stylist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private BarberService service;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    // Booking reference shown to the client, e.g. "#BB-8F3K2Q".
    @Column(unique = true)
    private String code;

    // Price (COP) at the moment of booking; later catalog price changes don't affect it.
    private Long price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
