package com.alopezme.hotrodtester.configuration;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class InfinispanConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public InfinispanRemoteCacheCustomizer customizer() {
        String xml = String.format("<infinispan>" + "   <cache-container>"
              + "       <distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\" statistics=\"true\">"
              + "           <encoding>" + "               <key media-type=\"application/x-protostream\"/>"
              + "               <value media-type=\"application/x-protostream\"/>" + "           </encoding>" +
              //                    "           <transaction mode=\"NONE\"/>" +
              //                    "           <expiration lifespan=\"-1\" max-idle=\"-1\" interval=\"60000\"/>" +
              //                    "           <memory storage=\"HEAP\"/>" +
              //                    "           <indexing enabled=\"true\">" +
              //                    "               <key-transformers/>" +
              //                    "               <indexed-entities/>" +
              //                    "           </indexing>" +
              //                    "           <state-transfer enabled=\"false\" await-initial-transfer=\"false\"/>" +
              //                    "           <partition-handling when-split=\"ALLOW_READ_WRITES\" merge-policy=\"REMOVE_ALL\"/>" +
              "       </distributed-cache>" + "   </cache-container>" + "</infinispan>", "indexed-cache");
        return b -> {
            b.remoteCache("sessions").templateName(DefaultTemplate.DIST_SYNC);
            b.remoteCache("indexed-cache").configuration(xml);
        };

    }
}
