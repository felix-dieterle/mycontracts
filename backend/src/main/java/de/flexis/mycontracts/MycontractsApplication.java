package de.flexis.mycontracts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MycontractsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MycontractsApplication.class, args);
    }
}
