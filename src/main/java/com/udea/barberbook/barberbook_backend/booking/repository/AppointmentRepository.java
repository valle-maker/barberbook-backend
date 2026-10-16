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
