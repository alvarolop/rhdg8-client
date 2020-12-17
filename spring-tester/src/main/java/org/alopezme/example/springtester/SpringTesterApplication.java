package org.alopezme.example.springtester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringTesterApplication {

   public static void main(String... args) {
      SpringApplication.run(SpringTesterApplication.class, args);
   }
}