package com.alopezme.hotrodtester.configuration;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.UTF8StringMarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AppConfiguration {

    @Autowired
    RemoteCacheManager remoteCacheManager;

    private final String BOOKS_CACHE_NAME = "books";
    @Value("${alvaro.queries.cache-name}")
    private String INDEXED_CACHE_NAME;

    @Bean
    RemoteCache<Integer, Book> defaultBooksCache(){
        return remoteCacheManager.getCache(BOOKS_CACHE_NAME);
    }

    @Bean
    RemoteCache<Integer, String> stringBooksCache(){
        DataFormat jsonString = DataFormat.builder()
                .valueType(MediaType.APPLICATION_JSON)
                .valueMarshaller(new UTF8StringMarshaller())
                .build();
        return remoteCacheManager.getCache(BOOKS_CACHE_NAME).withDataFormat(jsonString);

    }
    @Bean
    RemoteCache<Integer, Book> indexedBooksCache(){
        return remoteCacheManager.getCache(INDEXED_CACHE_NAME);
    }
}
