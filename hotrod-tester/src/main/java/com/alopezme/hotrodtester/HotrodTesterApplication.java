package me.ignaciosanchez.hotrodtester;

import org.infinispan.spring.remote.session.configuration.EnableInfinispanRemoteHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableInfinispanRemoteHttpSession(cacheName = "sessions")
public class HotrodTesterApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotrodTesterApplication.class, args);
	}
}
