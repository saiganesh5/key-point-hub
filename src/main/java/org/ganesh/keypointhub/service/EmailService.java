package org.ganesh.keypointhub.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailService {
private final JavaMailSender mailSender;

@Value("${spring.mail.username}")
private String from;

@Value("${report.mail.to}")
private String to;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendZipReport(File zipFile){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Daily KeyPointHUb Report");
            helper.setText("Attached is the ZIP archive containing pose data.",false);
            helper.addAttachment(zipFile.getName(),new FileSystemResource(zipFile));
            mailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Failed to send email",e);
        }
    }
}
