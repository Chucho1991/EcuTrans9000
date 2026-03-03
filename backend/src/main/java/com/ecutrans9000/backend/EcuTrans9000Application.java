package com.ecutrans9000.backend;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Componente publico de backend para EcuTrans9000Application.
 */
@SpringBootApplication
@EnableScheduling
public class EcuTrans9000Application {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guayaquil"));
        SpringApplication.run(EcuTrans9000Application.class, args);
    }
}
