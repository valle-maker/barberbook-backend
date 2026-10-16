package com.udea.barberbook.barberbook_backend.stylist.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;

import jakarta.persistence.LockModeType;

public interface StylistRepository extends JpaRepository<Stylist, UUID> {

    List<Stylist> findByActiveTrue();

    List<Stylist> findByActiveTrueAndServices_Id(UUID serviceId);

    // SELECT ... FOR UPDATE on the stylist rows, always in id order so concurrent
    // bookings that lock several stylists can't deadlock. Serializes bookings per stylist.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stylist s where s.id in :ids order by s.id")
    List<Stylist> lockAllByIdOrdered(@Param("ids") Collection<UUID> ids);
}
