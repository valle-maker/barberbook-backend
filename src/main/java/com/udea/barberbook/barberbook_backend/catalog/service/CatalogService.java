package com.udea.barberbook.barberbook_backend.catalog.service;

import java.util.List;
import java.util.UUID;

import com.udea.barberbook.barberbook_backend.catalog.domain.ServiceStatus;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceRequest;
import com.udea.barberbook.barberbook_backend.catalog.dto.ServiceResponse;

public interface CatalogService {

    List<ServiceResponse> listActive();

    // statusFilter null = all services, active and inactive
    List<ServiceResponse> listAll(ServiceStatus statusFilter);

    ServiceResponse create(ServiceRequest request);

    ServiceResponse update(UUID id, ServiceRequest request);

    ServiceResponse changeStatus(UUID id, ServiceStatus status);
}
