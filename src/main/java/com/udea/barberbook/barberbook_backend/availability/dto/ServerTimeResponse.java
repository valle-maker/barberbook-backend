package com.udea.barberbook.barberbook_backend.availability.dto;

// Server clock in the business time zone; lets the front count down / validate
// "today" against the backend's time instead of the browser's.
public record ServerTimeResponse(long timestamp, String timeZone) {}
