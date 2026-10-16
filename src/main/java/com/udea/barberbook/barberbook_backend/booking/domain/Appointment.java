package com.udea.barberbook.barberbook_backend.booking.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;

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

// Minimal booking record: HU-12 reads it to subtract busy time from a stylist's
// day. HU-13 will extend it (client, price snapshot, confirmation code).
// Times are local to the business time zone (app.business.zone).
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

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
}
