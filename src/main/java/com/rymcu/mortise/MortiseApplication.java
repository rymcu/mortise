package com.rymcu.mortise;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@EnableEncryptableProperties
@SpringBootApplication
public class MortiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MortiseApplication.class, args);
    }

}