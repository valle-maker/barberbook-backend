package com.udea.barberbook.barberbook_backend.catalog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.udea.barberbook.barberbook_backend.catalog.domain.BarberService;
import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;

public interface BarberServiceRepository extends JpaRepository<BarberService, UUID> {

    List<BarberService> findAllByOrderByNameAsc();

    List<BarberService> findByStatusOrderByNameAsc(ServiceStatus status);
}
