package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DigitalBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalBankingApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserAfterStartup() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();
            if (os.contains("win")) {
                rt.exec("rundll32 url.dll,FileProtocolHandler http://localhost:8080");
            } else if (os.contains("mac")) {
                rt.exec("open http://localhost:8080");
            } else if (os.contains("nix") || os.contains("nux")) {
                rt.exec("xdg-open http://localhost:8080");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}