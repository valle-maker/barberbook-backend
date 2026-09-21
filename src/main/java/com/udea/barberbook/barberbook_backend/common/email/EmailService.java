package com.udea.barberbook.barberbook_backend.common.email;

public interface EmailService {

    void sendTemporaryCredentials(String toEmail, String fullName, String temporaryPassword);
}
