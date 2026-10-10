package com.gui.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class FinancasApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancasApplication.class, args);
    }

    /** Relógio injetável: facilita testar regras que dependem da data de hoje. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
