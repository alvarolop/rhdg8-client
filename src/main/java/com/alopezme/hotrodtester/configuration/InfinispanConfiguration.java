package com.alopezme.hotrodtester.configuration;

import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.GenericTransactionManagerLookup;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Configuration
public class InfinispanConfiguration {

    Logger logger = LoggerFactory.getLogger(InfinispanConfiguration.class);

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public InfinispanRemoteCacheCustomizer infinispanRemoteCacheCustomizer() {
        return b -> {
            logger.info("Start method infinispanRemoteCacheCustomizer()");
            b.marshaller(new ProtoStreamMarshaller());
            b.marshaller(new JavaSerializationMarshaller());
            b.addJavaSerialAllowList(".*");
            b.addContextInitializer(new BookSchemaImpl());
            b.transaction().transactionTimeout(1, TimeUnit.MINUTES);
            b.remoteCache(CacheNames.SESSIONS_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.TESTER_CACHE_NAME + ".yaml"))
                    .marshaller(JavaSerializationMarshaller.class);
            b.remoteCache(CacheNames.TESTER_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.TESTER_CACHE_NAME + ".yaml"))
                    .marshaller(JavaSerializationMarshaller.class);
            b.remoteCache(CacheNames.BOOKS_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.BOOKS_CACHE_NAME + ".yaml"))
                    .marshaller(JavaSerializationMarshaller.class);
            b.remoteCache(CacheNames.PROTO_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.PROTO_CACHE_NAME + ".yaml"))
                    .marshaller(ProtoStreamMarshaller.class);
            b.remoteCache(CacheNames.INDEXED_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.INDEXED_CACHE_NAME + ".yaml"))
                    .marshaller(ProtoStreamMarshaller.class);
            b.remoteCache(CacheNames.TRANSACTIONAL_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.TRANSACTIONAL_CACHE_NAME + ".yaml"))
                    .marshaller(ProtoStreamMarshaller.class)
                    .transactionMode(TransactionMode.NON_XA)
                    .transactionManagerLookup(GenericTransactionManagerLookup.getInstance());
            b.remoteCache(CacheNames.TRANSACTIONAL_CACHE_NAME2)
                    .configurationURI(URI.create("caches/" + CacheNames.TRANSACTIONAL_CACHE_NAME + ".yaml"))
                    .marshaller(ProtoStreamMarshaller.class)
                    .transactionMode(TransactionMode.NON_XA)
                    .transactionManagerLookup(GenericTransactionManagerLookup.getInstance());
        };
    }
}
