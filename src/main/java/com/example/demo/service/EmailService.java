package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInterviewInvitation(String toEmail, String candidateName, String interviewDate, String interviewLocation) {
        String subject = "Interview Invitation from Devstringx Pvt Ltd";
        String htmlBody = getEmailBody(candidateName, interviewDate, interviewLocation);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String getEmailBody(String candidateName, String interviewDate, String interviewLocation) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Interview Invitation</title>
                <style>
                    body {
                        font-family: 'Segoe UI', sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 0 10px rgba(0,0,0,0.08);
                        padding: 30px;
                    }
                    .header {
                        background-color: #0d6efd;
                        padding: 20px;
                        border-radius: 8px 8px 0 0;
                        color: #ffffff;
                        text-align: center;
                    }
                    .header h2 {
                        margin: 0;
                    }
                    .content {
                        padding: 20px 0;
                        line-height: 1.6;
                        color: #333333;
                    }
                    .content p {
                        margin: 10px 0;
                    }
                    .location-box {
                        background-color: #f1f1f1;
                        padding: 15px;
                        border-left: 4px solid #0d6efd;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        font-size: 12px;
                        color: #999999;
                        text-align: center;
                        margin-top: 30px;
                    }
                </style>
            </head>
            <body>
            <div class="email-container">
                <div class="header">
                    <h2>Interview Invitation</h2>
                </div>
                <div class="content">
                    <p>Dear <strong>%s</strong>,</p>
                    <p>We are pleased to inform you that your resume has been shortlisted. You are invited to attend an interview on <strong>%s</strong>.</p>
                    <div class="location-box">
                        <strong>Interview Location:</strong><br>
                        %s
                    </div>
                    <p>We look forward to meeting you.</p>
                    <p class="footer">
                        This is a system-generated email. Please do not reply to this message.
                    </p>
                </div>
            </div>
            </body>
            </html>
        """, candidateName, interviewDate, interviewLocation);
    }
}
