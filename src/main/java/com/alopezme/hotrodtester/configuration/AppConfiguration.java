package com.alopezme.hotrodtester.configuration;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.UTF8StringMarshaller;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AppConfiguration {

    @Autowired
    RemoteCacheManager remoteCacheManager;

    Logger logger = LoggerFactory.getLogger(AppConfiguration.class);

    DataFormat jsonStringFormat = DataFormat.builder()
            .valueType(MediaType.APPLICATION_JSON)
            .valueMarshaller(new UTF8StringMarshaller())
            .build();

    @Bean
    RemoteCache<Integer, Book> serializationBooksCache(){
        return remoteCacheManager.getCache(CacheNames.BOOKS_CACHE_NAME);
    }

    @Bean
    RemoteCache<Integer, String> stringBooksCache(){
        return remoteCacheManager.getCache(CacheNames.BOOKS_CACHE_NAME).withDataFormat(jsonStringFormat);
    }

    @Bean
    RemoteCache<Integer, Book> protostreamBooksCache(){
        return remoteCacheManager.getCache(CacheNames.PROTO_CACHE_NAME);
    }

    @Bean
    RemoteCache<Integer, Book> indexedBooksCache(){
        return remoteCacheManager.getCache(CacheNames.INDEXED_CACHE_NAME);
    }

    @Bean
    RemoteCache<String, Byte[]> byteTesterCache(){
        return remoteCacheManager.getCache(CacheNames.TESTER_CACHE_NAME);
    }

    @Bean
    RemoteCache<String, String> stringTesterCache(){
        return remoteCacheManager.getCache(CacheNames.TESTER_CACHE_NAME);
    }

    @Bean
    RemoteCache<Integer, Book> transactionalBooksCache()  {
        return remoteCacheManager.getCache(CacheNames.TRANSACTIONAL_CACHE_NAME);
    }

    @Bean
    RemoteCache<Integer, Book> transactionalBooksCache2()  {
        return remoteCacheManager.getCache(CacheNames.TRANSACTIONAL_CACHE_NAME2);
    }

    /**
     * Register generated Protobuf schema with Infinispan Server.
     * This requires the RemoteCacheManager to be initialized.
     *
     * @param schema The serialization context initializer for the schema.
     */
    private void registerSchema(GeneratedSchema schema) {
        // Store schemas in the '___protobuf_metadata' cache to register them.
        // Using ProtobufMetadataManagerConstants might require the query dependency.
        final RemoteCache<String, String> protoMetadataCache = remoteCacheManager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
        // Add the generated schema to the cache.
        protoMetadataCache.put(schema.getProtoFileName(), schema.getProtoFile());

        // Ensure the registered Protobuf schemas do not contain errors.
        // Throw an exception if errors exist.
        String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors: " + errors + "\nSchema :\n" + schema.getProtoFileName());
        }
    }
}
