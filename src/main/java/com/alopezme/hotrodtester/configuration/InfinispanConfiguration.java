package com.alopezme.hotrodtester.configuration;

import com.alopezme.hotrodtester.model.Book;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;

@Configuration
public class InfinispanConfiguration {

    @Autowired
    RemoteCacheManager remoteCacheManager;

    @Value("${alvaro.queries.cache-name}")
    private String cacheName;

    @Bean
    RemoteCache<Integer,Book> remoteBookCache(){

        String xml = String.format(
                "<infinispan>" +
                        "   <cache-container>" +
                        "       <distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\" statistics=\"true\">" +
                        "           <encoding>" +
                        "               <key media-type=\"application/x-protostream\"/>" +
                        "               <value media-type=\"application/x-protostream\"/>" +
                        "           </encoding>" +
//                    "           <transaction mode=\"NONE\"/>" +
//                    "           <expiration lifespan=\"-1\" max-idle=\"-1\" interval=\"60000\"/>" +
//                    "           <memory storage=\"HEAP\"/>" +
//                    "           <indexing enabled=\"true\">" +
//                    "               <key-transformers/>" +
//                    "               <indexed-entities/>" +
//                    "           </indexing>" +
//                    "           <state-transfer enabled=\"false\" await-initial-transfer=\"false\"/>" +
//                    "           <partition-handling when-split=\"ALLOW_READ_WRITES\" merge-policy=\"REMOVE_ALL\"/>" +
                        "       </distributed-cache>" +
                        "   </cache-container>" +
                        "</infinispan>", cacheName);

        // Create books cache, if such does not exist
        return remoteCacheManager.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache(cacheName, new XMLStringConfiguration(xml));

    }

    @Bean
    RemoteCache<String, Session> sessionsCache(){

        String cacheName = "sessions";

        // Create books cache, if such does not exist
        return remoteCacheManager.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache(cacheName, DefaultTemplate.DIST_SYNC);

    }

}
