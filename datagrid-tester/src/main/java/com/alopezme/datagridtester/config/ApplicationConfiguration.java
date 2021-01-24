package com.alopezme.datagridtester.config;

import com.alopezme.datagridtester.model.Book;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Autowired
    RemoteCacheManager remoteCacheManager;
    
    @Bean
    RemoteCache<Integer,Book> remoteBookCache(){

        //TODO externalize
        String cacheName = "books";

        // Create books cache, if such does not exist
        remoteCacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache(cacheName, DefaultTemplate.DIST_SYNC);

        return remoteCacheManager.getCache(cacheName);
        
    }

}
