package com.udea.barberbook.barberbook_backend.common.email;

public interface EmailService {

    void sendTemporaryCredentials(String toEmail, String fullName, String temporaryPassword);

    void sendAppointmentConfirmation(
        String toEmail, String fullName, String code, String serviceName, String stylistName,
        String whenText, long price, String address
    );

    // whenText is the appointment's date and time as it was (cancelled) or as it is now (rescheduled).
    void sendAppointmentCancelled(
        String toEmail, String fullName, String code, String serviceName, String stylistName, String whenText
    );

    void sendAppointmentRescheduled(
        String toEmail, String fullName, String code, String serviceName, String stylistName, String whenText
    );
}
