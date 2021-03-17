package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.impl.query.RemoteQuery;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

    @Autowired
    private RemoteCache<Integer, Book> defaultBooksCache;

    @Autowired
    private RemoteCache<Integer, String> stringBooksCache;

    @Autowired
    private RemoteCache<Integer, Book> indexedBooksCache;

    // The name of the Scripts cache.
    String SCRIPTS_METADATA_CACHE_NAME = "___script_cache";
    // The name of the Protobuf definitions cache.
    String PROTOBUF_METADATA_CACHE_NAME = "___protobuf_metadata";
    // All error status keys end with this suffix. This is also the name of the global error key.
    String ERRORS_KEY_SUFFIX = ".errors";
    // All protobuf definition source files must end with this suffix.
    String PROTO_KEY_SUFFIX = ".proto";

    Logger logger = LoggerFactory.getLogger(AdminController.class);



    /***
     * MANAGE CACHES
     */

    @GetMapping("/reset")
    public String reset() throws InterruptedException, ExecutionException, TimeoutException {

        cacheManager.stop();
        cacheManager.start();

        return "SpringCache Manager restarted" + System.lineSeparator();
    }
    @GetMapping("/cache")
    public String caches() {

        return cacheManager.getCacheNames().toString() + System.lineSeparator();
    }




    /***
     * MANAGE CACHE: STATISTICS
     */

    @GetMapping("/cache/{cache}/statistics/server")
    public String getCacheServerStatistics(
            @PathVariable(value = "cache") String cacheName) {
        return cacheManager.getNativeCacheManager().getCache(cacheName).serverStatistics().getStatsMap().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/statistics/client")
    public String getCacheClientStatistics(
            @PathVariable(value = "cache") String cacheName) {
        RemoteCacheClientStatisticsMXBean clientStatistics = cacheManager.getNativeCacheManager().getCache(cacheName).clientStatistics();

        String string = "{" + "\"remoteHits\":\"" + clientStatistics.getRemoteHits() + "\"," +
                "\"remoteMisses\":\"" + clientStatistics.getRemoteMisses() + "\"," +
                "\"remoteRemoves\":\"" + clientStatistics.getRemoteRemoves() + "\"," +
                "\"remoteStores\":\"" + clientStatistics.getRemoteStores() + "\"," +
                "\"averageRemoteReadTime\":\"" + clientStatistics.getAverageRemoteReadTime() + "\"," +
                "\"averageRemoteRemovesTime\":\"" + clientStatistics.getAverageRemoteRemovesTime() + "\"," +
                "\"averageRemoteStoreTime\":\"" + clientStatistics.getAverageRemoteStoreTime() + "\"," +
                "\"nearCacheHits\":\"" + clientStatistics.getNearCacheHits() + "\"," +
                "\"nearCacheMisses\":\"" + clientStatistics.getNearCacheMisses() + "\"," +
                "\"nearCacheInvalidations\":\"" + clientStatistics.getNearCacheInvalidations() + "\"," +
                "\"nearCacheSize\":\"" + clientStatistics.getNearCacheSize() + "\"," +
                "\"timeSinceReset\":\"" + clientStatistics.getTimeSinceReset() + "\"" +
                "}";// Close JSON
        return string + System.lineSeparator();
    }





    /***
     * REGISTER PROTOS AND SCRIPTS
     */

    @GetMapping("/register/proto")
    public String cacheRegisterProto() throws Exception {

        // Register Team schema in the server
        Path protoPath = Paths.get(RemoteQuery.class.getClassLoader().getResource("proto/book.proto").toURI());
        String proto = Files.readString(protoPath);

//        logger.info("--> Proto schema: " + proto);

        RemoteCache<String, String> protoCache = cacheManager.getNativeCacheManager().getCache(PROTOBUF_METADATA_CACHE_NAME);

        logger.info("--> REMOVE");
        protoCache.remove("book.proto");
        logger.info("--> PUT");
        protoCache.put("book.proto", proto);
        logger.info("--> END");

        String errors = protoCache.get(ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors:\n" + errors);
        }

        return "Protobuf cache now contains " + protoCache.entrySet().size() + " entries: " + System.lineSeparator() +
                protoCache.entrySet().toString() + System.lineSeparator();
    }

    @GetMapping("/register/script")
    public String cacheRegisterScripts()  {

        String script = "// mode=local,language=javascript\n"
                + "var cache = cacheManager.getCache(cacheName);\n"
                + "cache.put(key, value);";

        RemoteCache<String, String> scriptCache = cacheManager.getNativeCacheManager().getCache(SCRIPTS_METADATA_CACHE_NAME);
        scriptCache.put("putEntries.js", script);

        return "Scripts cache now contains " + scriptCache.entrySet().size() + " entries: " + System.lineSeparator() +
                scriptCache.entrySet().toString() + System.lineSeparator();

    }



    /***
     * MANAGE BOOKS CACHE: SIZE, LOAD, ETC.
     */

    @GetMapping("/{cache}/size")
    public String getSize(
            @PathVariable(value = "cache") String cacheName
    )  {
        return  cacheManager.getNativeCacheManager().getCache(cacheName).entrySet().size() + System.lineSeparator();
    }



    @GetMapping("/test-cache1")
    public String testCache1() throws JsonProcessingException {
        logger.info("GET 1 : " + defaultBooksCache.get(0).toString());
        return  defaultBooksCache.entrySet().size() + System.lineSeparator();
    }

    @GetMapping("/test-cache2")
    public String testCache2() {
        logger.info("GET 2 : " + stringBooksCache.get(0).toString());
        return  stringBooksCache.entrySet().size() + System.lineSeparator();
    }

    @GetMapping("/test-cache3")
    public String testCache3() throws JsonProcessingException {
        logger.info("GET 3 : " + indexedBooksCache.get(0).toString());
        return  indexedBooksCache.entrySet().size() + System.lineSeparator();
    }
}
