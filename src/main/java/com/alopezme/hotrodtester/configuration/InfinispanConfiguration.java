package com.alopezme.hotrodtester.configuration;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class InfinispanConfiguration {

    private final String BOOKS_CACHE_NAME = "books";
    private final String TESTER_CACHE_NAME = "tester";
    private final String SESSIONS_CACHE_NAME = "sessions";
    private final String TRANSACTIONAL_CACHE_NAME = "books-transactional";
    private final String INDEXED_CACHE_NAME = "books-indexed";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public InfinispanRemoteCacheCustomizer infinispanRemoteCacheCustomizer() {
        return b -> {
            b.marshaller(new ProtoStreamMarshaller());
            b.marshaller(new JavaSerializationMarshaller());
            b.addJavaSerialWhiteList(".*");
            b.addContextInitializer(new BookSchemaImpl());
            b.remoteCache(SESSIONS_CACHE_NAME).templateName(DefaultTemplate.DIST_SYNC);
            b.remoteCache(TESTER_CACHE_NAME).configurationURI(URI.create("caches/tester.xml"));
            b.remoteCache(BOOKS_CACHE_NAME).configurationURI(URI.create("caches/books-javaser.xml"));
            b.remoteCache(INDEXED_CACHE_NAME).configurationURI(URI.create("caches/books-indexed.xml"));
            b.remoteCache(TRANSACTIONAL_CACHE_NAME)
                .configurationURI(URI.create("caches/books-transactional.xml"));
//                .transactionManagerLookup(GenericTransactionManagerLookup.INSTANCE)
//                .transactionMode(TransactionMode.NON_XA);

        };
    }
}
