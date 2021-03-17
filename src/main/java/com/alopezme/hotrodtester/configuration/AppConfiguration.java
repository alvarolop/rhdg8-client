package com.alopezme.hotrodtester.configuration;

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

    @Bean
    RemoteCache<Integer,String> remoteStringCache(
            @Value("${alvaro.queries.cache-name}") String cacheName
    ){

        DataFormat jsonString = DataFormat.builder()
                .valueType(MediaType.APPLICATION_JSON)
                .valueMarshaller(new UTF8StringMarshaller())
                .build();

        return remoteCacheManager.getCache(cacheName).withDataFormat(jsonString);

    }

}
