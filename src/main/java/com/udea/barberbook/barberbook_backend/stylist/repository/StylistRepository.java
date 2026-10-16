package com.udea.barberbook.barberbook_backend.stylist.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;

public interface StylistRepository extends JpaRepository<Stylist, UUID> {

    List<Stylist> findByActiveTrueAndServices_Id(UUID serviceId);
}
