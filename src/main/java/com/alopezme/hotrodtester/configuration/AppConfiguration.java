package com.alopezme.hotrodtester.configuration;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.UTF8StringMarshaller;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.transaction.TransactionManager;

@Component
public class AppConfiguration {

    @Autowired
    RemoteCacheManager remoteCacheManager;

    private final String BOOKS_CACHE_NAME = "books";
    private final String TESTER_CACHE_NAME = "tester";
    private final String TRANSACTIONAL_CACHE_NAME = "books-transactional";
    private final String INDEXED_CACHE_NAME = "books-indexed";

    DataFormat javaSerialization = DataFormat.builder()
            .keyType(MediaType.APPLICATION_SERIALIZED_OBJECT)
            .valueType(MediaType.APPLICATION_SERIALIZED_OBJECT)
            .build();

    DataFormat jsonStringFormat = DataFormat.builder()
            .valueType(MediaType.APPLICATION_JSON)
            .valueMarshaller(new UTF8StringMarshaller())
            .build();

    DataFormat protobufFormat = DataFormat.builder()
            .keyType(MediaType.APPLICATION_PROTOSTREAM)
            .valueType(MediaType.APPLICATION_PROTOSTREAM)
            .build();

    @Bean
    RemoteCache<Integer, Book> defaultBooksCache(){
        return remoteCacheManager.getCache(BOOKS_CACHE_NAME).withDataFormat(javaSerialization);
    }

    @Bean
    RemoteCache<Integer, String> stringBooksCache(){
        return remoteCacheManager.getCache(BOOKS_CACHE_NAME).withDataFormat(jsonStringFormat);
    }

    @Bean
    RemoteCache<Integer, Book> indexedBooksCache(){
        return remoteCacheManager.getCache(INDEXED_CACHE_NAME).withDataFormat(protobufFormat);
    }

    @Bean
    RemoteCache<String, Byte[]> byteTesterCache(){
        return remoteCacheManager.getCache(TESTER_CACHE_NAME).withDataFormat(javaSerialization);
    }

    @Bean
    RemoteCache<String, String> stringTesterCache(){
        return remoteCacheManager.getCache(TESTER_CACHE_NAME);
    }

    @Bean
    RemoteCache<Integer, Book> transactionalBooksCache(){
        return remoteCacheManager.getCache(TRANSACTIONAL_CACHE_NAME).withDataFormat(protobufFormat);
    }
}
