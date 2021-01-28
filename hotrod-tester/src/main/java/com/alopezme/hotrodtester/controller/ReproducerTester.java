package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.utils.QueriesCacheManager;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reproducer")
public class ReproducerTester {

    @Autowired
    RemoteCacheManager remoteCacheManager;

    @Autowired
    QueriesCacheManager queriesCacheManager;

    String cacheName = "test-reproducer";
//    String xml = String.format(
//            "<infinispan>" +
//            "   <cache-container>" +
//            "       <distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\" statistics=\"true\">" +
//            "           <encoding>" +
//            "               <key media-type=\"application/x-protostream\"/>" +
//            "               <value media-type=\"application/x-protostream\"/>" +
//            "           </encoding>" +
//            "           <transaction mode=\"NONE\"/>" +
//            "           <expiration lifespan=\"-1\" max-idle=\"-1\" interval=\"60000\"/>" +
//            "           <memory storage=\"HEAP\"/>" +
//            "           <indexing enabled=\"true\">" +
//            "               <key-transformers/>" +
//            "               <indexed-entities/>" +
//            "           </indexing>" +
//            "           <state-transfer enabled=\"false\" await-initial-transfer=\"false\"/>" +
//            "           <partition-handling when-split=\"ALLOW_READ_WRITES\" merge-policy=\"REMOVE_ALL\"/>" +
//            "       </distributed-cache>" +
//            "   </cache-container>" +
//            "</infinispan>", cacheName);

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

    Logger logger = LoggerFactory.getLogger(QueriesTester.class);

    @GetMapping("test")
    public String loadBooksCacheTest() throws Exception{

        RemoteCache<Integer, Book> cache = queriesCacheManager.getManager().administration()
                .getOrCreateCache(cacheName, new XMLStringConfiguration(xml));

        logger.info("\n--> Test begins <--\n");
        logger.info("Content of entry #100: " + cache.get(100) + "\n");

        // This step will work correctly, as entry #100 is empty
        logger.info("Put entry #100");
        cache.put (100, new Book(100, "Alvaro", "Lopez", 1993));
        logger.info("Content of entry #100: " + cache.get(100) + "\n");

        return "";
    }

    @GetMapping("remove-cache")
    public String removeTestCache() throws Exception{

        queriesCacheManager.getManager().administration().removeCache(cacheName);
        return "";
    }
}
