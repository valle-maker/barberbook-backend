package com.udea.barberbook.barberbook_backend.booking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.udea.barberbook.barberbook_backend.booking.domain.Appointment;
import com.udea.barberbook.barberbook_backend.booking.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByCode(String code);

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
