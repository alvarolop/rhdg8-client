package com.alopezme.hotrodtester.configuration;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class InfinispanConfiguration {

    private final String BOOKS_CACHE_NAME = "books";
    private final String TESTER_CACHE_NAME = "tester";
    private final String SESSIONS_CACHE_NAME = "sessions";

    @Value("${alvaro.queries.cache-name}")
    private String INDEXED_CACHE_NAME;

    String xmlSerialized = "<infinispan>" +
                    "   <cache-container>" +
                    "       <distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\" statistics=\"true\">" +
                    "           <encoding>" +
                    "               <key media-type=\"application/x-java-serialized-object\"/>" +
                    "               <value media-type=\"application/x-java-serialized-object\"/>" +
                    "           </encoding>" +
                    "           <transaction mode=\"NONE\"/>" +
                    "           <expiration lifespan=\"-1\" max-idle=\"-1\" interval=\"60000\"/>" +
                    "           <memory storage=\"HEAP\"/>" +
                    "           <state-transfer enabled=\"false\" await-initial-transfer=\"false\"/>" +
                    "           <partition-handling when-split=\"ALLOW_READ_WRITES\" merge-policy=\"REMOVE_ALL\"/>" +
                    "       </distributed-cache>" +
                    "   </cache-container>" +
                    "</infinispan>";

    String xmlProtoStream = "<infinispan>" +
                    "   <cache-container>" +
                    "       <distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\" statistics=\"true\">" +
                    "           <encoding>" +
                    "               <key media-type=\"application/x-protostream\"/>" +
                    "               <value media-type=\"application/x-protostream\"/>" +
                    "           </encoding>" +
                    "           <transaction mode=\"NONE\"/>" +
                    "           <expiration lifespan=\"-1\" max-idle=\"-1\" interval=\"60000\"/>" +
                    "           <memory storage=\"HEAP\"/>" +
                    "           <indexing enabled=\"true\">" +
                    "               <key-transformers/>" +
                    "               <indexed-entities/>" +
                    "           </indexing>" +
                    "           <state-transfer enabled=\"false\" await-initial-transfer=\"false\"/>" +
                    "           <partition-handling when-split=\"ALLOW_READ_WRITES\" merge-policy=\"REMOVE_ALL\"/>" +
                    "       </distributed-cache>" +
                    "   </cache-container>" +
                    "</infinispan>";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public InfinispanRemoteCacheCustomizer infinispanRemoteCacheCustomizer() {
        return b -> {
            b.marshaller(new JavaSerializationMarshaller());
            b.marshaller(new ProtoStreamMarshaller());
            b.addJavaSerialWhiteList(".*");
            b.addContextInitializer(new BookSchemaImpl());
            b.remoteCache(SESSIONS_CACHE_NAME).templateName(DefaultTemplate.DIST_SYNC);
            b.remoteCache(BOOKS_CACHE_NAME).configuration(String.format(xmlSerialized, BOOKS_CACHE_NAME));
            b.remoteCache(TESTER_CACHE_NAME).configuration(String.format(xmlSerialized, TESTER_CACHE_NAME));
            b.remoteCache(INDEXED_CACHE_NAME).configuration(String.format(xmlProtoStream, INDEXED_CACHE_NAME));
        };
    }
}
