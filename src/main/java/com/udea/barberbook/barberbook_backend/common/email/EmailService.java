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

    // HU-20: the shop cancelled the appointment (contingency). Apologises and invites to rebook.
    // reason may be null or blank.
    void sendAppointmentCancelledByShop(
        String toEmail, String fullName, String code, String serviceName, String stylistName, String whenText,
        String reason
    );

    // HU-20: same day and time, another stylist.
    void sendAppointmentReassigned(
        String toEmail, String fullName, String code, String serviceName, String previousStylistName,
        String newStylistName, String whenText
    );
}
