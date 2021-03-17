package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

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

    /**
     * GET KEYS
     */
    @GetMapping("/cache/{cache}/keys")
    public String getKeys(
            @PathVariable(value = "cache") String cacheName) {

        return cacheManager.getNativeCacheManager().getCache(cacheName).keySet().toString() + System.lineSeparator();
    }


    /**
     * PUT ENTRIES
     */
    @PutMapping("/cache/{cache}/bytes")
    public String putByte (
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "async", defaultValue = "false") boolean isAsync,
            @RequestParam(value = "asyncTime", defaultValue = "500") int asyncTime,
            @RequestParam(value = "size", defaultValue = "1024") int entrySize,
            @RequestParam(value = "minkey", defaultValue = "0") int minKey) throws Exception {

        RemoteCache<String, byte[]> cache = cacheManager.getNativeCacheManager().getCache(cacheName);

        byte[] bytes = new byte[entrySize];
        Random rnd = new Random();

        int maxKey = minKey + numEntries;
        for (int i = minKey; i < maxKey; i++) {

            rnd.nextBytes(bytes);
            if (isAsync) {
                cache.put(Integer.toString(i), bytes, asyncTime, TimeUnit.MILLISECONDS);
                logger.info("Put byte " + i + " async");
            } else {
                cache.put(Integer.toString(i), bytes);
                logger.info("put byte " + i + "  sync");
            }
        }

        return "PUT Byte [" + minKey + " , " + maxKey + ")" + System.lineSeparator();
    }

    @PutMapping("/cache/{cache}/string")
    public String putString (
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "minkey", defaultValue = "0") int minKey,
            @RequestParam(value = "entrycontent", defaultValue = "Test") String entryContent) throws Exception {

        RemoteCache<String, String> cache = cacheManager.getNativeCacheManager().getCache(cacheName);
        int maxKey = minKey + numEntries;

        for (int i = minKey; i < maxKey; i++) {
            cache.put(Integer.toString(i), entryContent + " " + Integer.toString(i));
            logger.info("Put string " + i);
        }
        return "PUT String [" + minKey + " , " + maxKey + ")" + System.lineSeparator();
    }







    /**
     * GET ENTRIES
     */
    @GetMapping("/cache/{cache}/bulk")
    public String getBulk (
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "async", defaultValue = "false") boolean isAsync,
            @RequestParam(value = "asyncTime", defaultValue = "500") int asyncTime,
            @RequestParam(value = "minkey", defaultValue = "0") int minKey) throws Exception {

        RemoteCache<String, byte[]> cache = cacheManager.getNativeCacheManager().getCache(cacheName);

        for (int i = minKey; i < (minKey + numEntries); i++) {
            if (isAsync) {
                cache.getAsync(Integer.toString(i)).get(asyncTime, TimeUnit.MILLISECONDS);
                logger.info("get ok " + i + " Async");
            } else {
                cache.get(Integer.toString(i));
                logger.info("get ok " + i);
            }

        }

        return "OK " + numEntries + " " + minKey + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/byte")
    public String getByte(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "key") int key,
            @RequestParam(value = "show", defaultValue = "false") boolean show) {

        RemoteCache<String, byte[]> cache = cacheManager.getNativeCacheManager().getCache(cacheName);
        String result = Arrays.toString(cache.get(Integer.toString(key))) + System.lineSeparator();

        return show ? result : "OK" + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/string")
    public String getString(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "key") int key,
            @RequestParam(value = "show", defaultValue = "false") boolean show) {

        RemoteCache<String, String> cache = cacheManager.getNativeCacheManager().getCache(cacheName);
        String result = cache.get(Integer.toString(key)) + System.lineSeparator();

        return show ? result : "OK" + System.lineSeparator();
    }





    /**
     * REMOVE ENTRIES
     */

    @DeleteMapping("/cache/{cache}")
    public String removeEntries(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "minkey", required = false, defaultValue = "0") int minKey) {

        RemoteCache<String, byte[]> cache = cacheManager.getNativeCacheManager().getCache(cacheName);
        int maxKey = minKey + numEntries;

        for (int i = minKey; i < maxKey; i++) {
            cache.remove(Integer.toString(i));
        }

        return "Deleted from " + minKey + " to " + maxKey + System.lineSeparator();
    }









    /***
     * REGISTER PROTOS AND SCRIPTS
     */

    @GetMapping("/cache/register/proto")
    public String cacheRegisterProto() throws Exception {

        // Register Team schema in the server
        Path protoPath = Paths.get(RemoteQuery.class.getClassLoader().getResource("proto/book.proto").toURI());
        String proto = Files.readString(protoPath);

//        logger.info("--> Proto schema: " + proto);

        RemoteCache<String, String> protoCache = cacheManager.getNativeCacheManager().getCache(PROTOBUF_METADATA_CACHE_NAME);

        if (!protoCache.containsKey("book.proto")) {
            protoCache.put("book.proto", proto);
        }

        String errors = protoCache.get(ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors:\n" + errors);
        }

        return "Protobuf cache now contains " + protoCache.entrySet().size() + " entries: " + System.lineSeparator() +
                protoCache.entrySet().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/register/script")
    public String cacheRegisterScripts()  {

        String script = "// mode=local,language=javascript\n"
                + "var cache = cacheManager.getCache(cacheName);\n"
                + "cache.put(key, value);";

        RemoteCache<String, String> scriptCache = cacheManager.getNativeCacheManager().getCache(SCRIPTS_METADATA_CACHE_NAME);
        if (!scriptCache.containsKey("putEntries.js")) {
            scriptCache.put("putEntries.js", script);
        }

        return "Scripts cache now contains " + scriptCache.entrySet().size() + " entries: " + System.lineSeparator() +
                scriptCache.entrySet().toString() + System.lineSeparator();

    }



    /***
     * MANAGE BOOKS CACHE: SIZE, LOAD, ETC.
     */

    @GetMapping("/book/size")
    public String getAll()  {
        return  cacheManager.getNativeCacheManager().getCache("books").entrySet().size() + System.lineSeparator();
    }

    @GetMapping("/book/load")
    public String loadBooksCache() throws IOException {

        RemoteCache< Integer, Book> cache = cacheManager.getNativeCacheManager().getCache("books");
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/books.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.info("PUT : " + mapper.writeValueAsString(book));
                cache.put(book.getId(), book);
            }
        }
        return "Books cache now contains " + cache.entrySet().size() + " entries";
    }

    @GetMapping("/book/reduced-load")
    public String miniLoadBooksCache() throws IOException {

        RemoteCache< Integer, Book> cache = cacheManager.getNativeCacheManager().getCache("books");
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/books.csv")))) {
            String line;
            int iteration = 0;
            while ((line = br.readLine()) != null && iteration < 100) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.info("PUT : " + mapper.writeValueAsString(book));
                cache.put(book.getId(), book);
                iteration++;
            }
        }
        return "Books cache now contains " + cache.entrySet().size() + " entries";
    }



}
