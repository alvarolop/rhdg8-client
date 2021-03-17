package com.alopezme.hotrodtester;

import org.infinispan.spring.remote.session.configuration.EnableInfinispanRemoteHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableInfinispanRemoteHttpSession
public class HotRodTesterApplication {

   public static void main(String... args) {
      SpringApplication.run(HotRodTesterApplication.class, args);
   }
}