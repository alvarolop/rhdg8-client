package me.ignaciosanchez.hotrodtester.controller;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.MapSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("session")
public class SessionController {
	
	@Autowired
	SpringRemoteCacheManager springCacheManager;
	
    @Autowired
    RemoteCacheManager remoteCacheManager;

    Logger logger = LoggerFactory.getLogger(CacheController.class);
    
    @GetMapping("/current")
	String getSessionId(HttpSession session) {
		return session.getId() + System.lineSeparator();
	}
    
	@GetMapping("/current/expired")
	String isSessionExpired(HttpSession session) {
		// https://docs.spring.io/spring-session/docs/current/api/org/springframework/session/MapSession.html
		RemoteCache<String, MapSession> cache = remoteCacheManager.getCache("sessions");
		MapSession mapSession = cache.get(session.getId());
		return Boolean.toString(mapSession.isExpired()) + System.lineSeparator();
	}
	
	@GetMapping("/current/attributes")
	String attributes(HttpSession session) {
		logger.info("HttpSession ID: " + session.getId());

		// https://docs.spring.io/spring-session/docs/current/api/org/springframework/session/MapSession.html
		RemoteCache<String, MapSession> cache = remoteCacheManager.getCache("sessions");
		logger.info("HttpSession Cache RHDG: " + cache.keySet());
		
		return String.join(", ", cache.get(session.getId()).getAttributeNames()) + System.lineSeparator();
	}
	
	@GetMapping("/{id}/attributes")
	String attributes(@PathVariable(value = "id") String sessionID) {
		RemoteCache<String, MapSession> cache = remoteCacheManager.getCache("sessions");
		logger.info("HttpSession Cache RHDG: " + cache.keySet());
		
		return String.join(", ", cache.get(sessionID).getAttributeNames()) + System.lineSeparator();
	}
	

	@GetMapping(path = "/session-stats", produces="application/json")
	String sessionCount() {
		
		StringBuilder attributes = new StringBuilder("[");

		String hostname = System.getenv().getOrDefault("HOSTNAME", "Unknown");
		attributes.append("{\"hostname\":\"" + hostname + "\"}");
		attributes.append(",{");

		// https://access.redhat.com/webassets/avalon/d/red-hat-data-grid/7.3/java/org/infinispan/client/hotrod/ServerStatistics.html
		Map<String, String> stats = remoteCacheManager.getCache("sessions").serverStatistics().getStatsMap();
		
        Iterator<String> iterator = stats.keySet().iterator();
        while(iterator.hasNext()){
        	String key = iterator.next();
        	attributes.append("\"" + key + "\":\"" + stats.get(key) + "\"");
        	if (iterator.hasNext())
        		attributes.append(",");
        	else
        		attributes.append("}");
        }
        attributes.append("]");
		return attributes.toString() + System.lineSeparator();
	}
}
