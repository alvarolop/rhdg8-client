package com.alopezme.hotrodtester.queriesUtils;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.marshall.UTF8StringMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class QueriesCacheManager {

    @Value("${alvaro.queries.host}")
    private String host;
    @Value("${alvaro.queries.port}")
    private int port;
    @Value("${alvaro.queries.cache-name}")
    private String cacheName;

    private RemoteCacheManager remoteCacheManager;
    private RemoteCache<Integer, Book> remoteBookCache;
    private RemoteCache<Integer, String> remoteStringCache;
    Logger logger = LoggerFactory.getLogger(QueriesCacheManager.class);

    public QueriesCacheManager(){
    }

    @PostConstruct
    public void init() {
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .statistics()
                    .enable()
                .addServer()
                    .host(host)
                    .port(port)
                .security()
                    .authentication()
                        .saslMechanism("DIGEST-MD5")
                        .username("developer")
                        .password("developer")
                .marshaller(new ProtoStreamMarshaller())
                .addContextInitializers(new BookSchemaImpl());

        DataFormat jsonString = DataFormat.builder()
                .valueType(MediaType.APPLICATION_JSON)
                .valueMarshaller(new UTF8StringMarshaller())
                .build();

        this.remoteCacheManager = new RemoteCacheManager(configuration.build());
        this.remoteBookCache = remoteCacheManager.getCache(cacheName);
        this.remoteStringCache = remoteCacheManager.getCache(cacheName).withDataFormat(jsonString);

    }

    public RemoteCacheManager getManager() {
        return this.remoteCacheManager;
    }

    public RemoteCache<Integer, Book> getBookCache() {
        return this.remoteBookCache;
    }

    public RemoteCache<Integer, String> getStringCache() { return this.remoteStringCache; }

    public String getCacheName() {
        return cacheName;
    }
}
