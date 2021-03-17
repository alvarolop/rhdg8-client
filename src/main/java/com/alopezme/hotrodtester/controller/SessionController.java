package com.alopezme.hotrodtester.controller;

import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.session.MapSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("session")
public class SessionController {

    public static final String LATEST_SESSION_VALUE = "latest";
    @Autowired
    private SpringRemoteCacheManager cacheManager;

    Logger logger = LoggerFactory.getLogger(SessionController.class);

    @GetMapping("/")
    public String sessions() {
        return cacheManager.getCache("sessions").getNativeCache().keySet().toString();
    }

    @GetMapping("/{id}")
    public String sessionContent(@PathVariable String id) {
        SimpleValueWrapper simpleValueWrapper = (SimpleValueWrapper) cacheManager.getCache("sessions").get(id);
        if (simpleValueWrapper == null) {
            return "Session not found";
        }
        MapSession mapSession = (MapSession) simpleValueWrapper.get();
        return "Latest " + mapSession.getAttribute(LATEST_SESSION_VALUE);
    }
}
