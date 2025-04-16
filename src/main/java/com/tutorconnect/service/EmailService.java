package com.tutorconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // Send approval email to tutor
    public void sendApprovalEmail(String to, String tutorName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Tutor Application Approved");
        message.setText("Dear " + tutorName + ",\n\n"
                + "Your tutor application has been approved! "
                + "You can now sign up as a tutor on the platform.\n\n"
                + "Thank you!");
        javaMailSender.send(message);
    }

    // Send password reset or temporary password email to tutor
    public void sendPasswordResetEmail(String to, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Tutor Account Created - Temporary Password");
        message.setText("Dear Tutor,\n\n"
                + "Your tutor account has been created on the platform.\n\n"
                + "Your temporary password is: " + temporaryPassword + "\n\n"
                + "Please use this password to sign in and change your password upon login.\n\n"
                + "Thank you!");

        javaMailSender.send(message);
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}
