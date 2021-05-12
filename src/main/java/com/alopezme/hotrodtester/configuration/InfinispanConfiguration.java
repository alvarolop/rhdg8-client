package com.alopezme.hotrodtester.configuration;

import com.alopezme.hotrodtester.controller.AdminController;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Configuration
public class InfinispanConfiguration {

    Logger logger = LoggerFactory.getLogger(InfinispanConfiguration.class);

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public InfinispanRemoteCacheCustomizer infinispanRemoteCacheCustomizer() {
        return b -> {
            logger.warn("Start method infinispanRemoteCacheCustomizer()");
            b.transaction();
            b.marshaller(new ProtoStreamMarshaller());
            b.marshaller(new JavaSerializationMarshaller());
            b.addJavaSerialWhiteList(".*");
            b.addContextInitializer(new BookSchemaImpl());
            b.transaction().transactionTimeout(1, TimeUnit.MINUTES);
            b.remoteCache(CacheNames.SESSIONS_CACHE_NAME)
                    .templateName(DefaultTemplate.DIST_SYNC);
            b.remoteCache(CacheNames.TESTER_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.TESTER_CACHE_NAME + ".xml"))
                    .marshaller(JavaSerializationMarshaller.class);
            b.remoteCache(CacheNames.BOOKS_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.BOOKS_CACHE_NAME + ".xml"))
                    .marshaller(JavaSerializationMarshaller.class);
            b.remoteCache(CacheNames.PROTO_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.PROTO_CACHE_NAME + ".xml"))
                    .marshaller(ProtoStreamMarshaller.class);
            b.remoteCache(CacheNames.INDEXED_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.INDEXED_CACHE_NAME + ".xml"))
                    .marshaller(ProtoStreamMarshaller.class);
            b.remoteCache(CacheNames.TRANSACTIONAL_CACHE_NAME)
                    .configurationURI(URI.create("caches/" + CacheNames.TRANSACTIONAL_CACHE_NAME + ".xml"))
                    .marshaller(ProtoStreamMarshaller.class)
                    .transactionManagerLookup(RemoteTransactionManagerLookup.getInstance())
                    .transactionMode(TransactionMode.NON_XA);
        };
    }
}
