package com.udea.barberbook.barberbook_backend.stylist.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.udea.barberbook.barberbook_backend.stylist.domain.Stylist;

public interface StylistRepository extends JpaRepository<Stylist, UUID> {
}
