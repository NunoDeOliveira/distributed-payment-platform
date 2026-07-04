package com.tfg.ledgerservice;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter(
                new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
        );
    }
}
