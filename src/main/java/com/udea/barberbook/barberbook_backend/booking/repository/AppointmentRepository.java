package com.udea.barberbook.barberbook_backend.booking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;
import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByCode(String code);

    List<Appointment> findByClientIdAndStatusNot(UUID clientId, AppointmentStatus status);

    // "Mis reservas" (HU-14). Upcoming = confirmed and not finished yet (one in progress still
    // counts), soonest first.
    @Query("""
        select a from Appointment a
        where a.client.id = :clientId and a.status = :confirmed and a.endAt > :now
        order by a.startAt asc
        """)
    List<Appointment> findUpcoming(
        @Param("clientId") UUID clientId,
        @Param("confirmed") AppointmentStatus confirmed,
        @Param("now") LocalDateTime now
    );

    // History = cancelled, or already finished; most recent first.
    @Query("""
        select a from Appointment a
        where a.client.id = :clientId and (a.status = :cancelled or a.endAt <= :now)
        order by a.startAt desc
        """)
    List<Appointment> findHistory(
        @Param("clientId") UUID clientId,
        @Param("cancelled") AppointmentStatus cancelled,
        @Param("now") LocalDateTime now
    );

    // Confirmed appointments that start within [from, to) and have not finished yet (HU-20 list).
    @Query("""
        select a from Appointment a
        where a.status = :confirmed and a.endAt > :now and a.startAt >= :from and a.startAt < :to
        order by a.startAt asc
        """)
    List<Appointment> findActiveStartingBetween(
        @Param("confirmed") AppointmentStatus confirmed,
        @Param("now") LocalDateTime now,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    // Active services ordered by how many non-excluded appointments they have (most booked
    // first, then by name); services never booked still appear, at the end.
    @Query("""
        select s from BarberService s
        left join Appointment a on a.service = s and a.status <> :excluded
        where s.status = :serviceStatus
        group by s
        order by count(a) desc, s.name asc
        """)
    List<BarberService> findMostBooked(
        @Param("excluded") AppointmentStatus excluded,
        @Param("serviceStatus") ServiceStatus serviceStatus,
        Pageable pageable
    );

    // Appointments of the given stylists that overlap the [from, to) window.
    @Query("""
        select a from Appointment a
        where a.stylist.id in :stylistIds
          and a.status = :status
          and a.startAt < :to
          and a.endAt > :from
        """)
    List<Appointment> findOverlapping(
        @Param("stylistIds") Collection<UUID> stylistIds,
        @Param("status") AppointmentStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
