package org.ganesh.keypointhub.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DailyZipScheduler {
    private final EmailService emailService;

    public DailyZipScheduler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(cron="0 0 2 * * *")
    public void zipAndSend(){

        File zipFile=new File("exports/daily-data.zip");

        if(!zipFile.exists()){
            return;
        }

        emailService.sendZipReport(zipFile);
    }
}
