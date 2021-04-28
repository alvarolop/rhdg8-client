package com.alopezme.hotrodtester.configuration;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
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
    private final String TESTER_CACHE_NAME = "tester";
    @Value("${alvaro.queries.cache-name}")
    private String INDEXED_CACHE_NAME;

    @Bean
    RemoteCache<Integer, Book> defaultBooksCache(){
        DataFormat javaSerialization = DataFormat.builder()
                .keyType(MediaType.APPLICATION_SERIALIZED_OBJECT)
                .keyMarshaller(new JavaSerializationMarshaller())
                .valueType(MediaType.APPLICATION_SERIALIZED_OBJECT)
                .valueMarshaller(new JavaSerializationMarshaller())
                .build();
        return remoteCacheManager.getCache(BOOKS_CACHE_NAME).withDataFormat(javaSerialization);
    }

    @Bean
    RemoteCache<Integer, String> stringBooksCache(){
        DataFormat jsonStringFormat = DataFormat.builder()
                .valueType(MediaType.APPLICATION_JSON)
                .valueMarshaller(new UTF8StringMarshaller())
                .build();
        return remoteCacheManager.getCache(BOOKS_CACHE_NAME).withDataFormat(jsonStringFormat);

    }

    @Bean
    RemoteCache<Integer, Book> indexedBooksCache(){
        DataFormat protobufFormat = DataFormat.builder()
                .keyType(MediaType.APPLICATION_PROTOSTREAM)
                .keyMarshaller(new ProtoStreamMarshaller())
                .valueType(MediaType.APPLICATION_PROTOSTREAM)
                .valueMarshaller(new ProtoStreamMarshaller())
                .build();
        return remoteCacheManager.getCache(INDEXED_CACHE_NAME).withDataFormat(protobufFormat);
    }

    @Bean
    RemoteCache<String, Byte[]> byteTesterCache(){
        DataFormat javaSerialization = DataFormat.builder()
                .keyType(MediaType.APPLICATION_SERIALIZED_OBJECT)
                .keyMarshaller(new JavaSerializationMarshaller())
                .valueType(MediaType.APPLICATION_SERIALIZED_OBJECT)
                .valueMarshaller(new JavaSerializationMarshaller())
                .build();
        return remoteCacheManager.getCache(TESTER_CACHE_NAME).withDataFormat(javaSerialization);
    }

    @Bean
    RemoteCache<String, String> stringTesterCache(){
        return remoteCacheManager.getCache(TESTER_CACHE_NAME);
    }
}
