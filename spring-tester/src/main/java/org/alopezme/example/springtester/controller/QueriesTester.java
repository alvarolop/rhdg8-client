package org.alopezme.example.springtester.controller;

import org.alopezme.example.springtester.bean.QueriesCacheManager;
import org.alopezme.example.springtester.initializer.RemoteQueryInitializerImpl;
import org.alopezme.example.springtester.model.Book;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.impl.query.RemoteQuery;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("query")
public class QueriesTester {

    @Autowired
    SpringRemoteCacheManager springRemoteCacheManager;

    @Autowired
    RemoteCacheManager remoteCacheManager;

    @Autowired
    QueriesCacheManager queriesCacheManager;

    // The name of the Scripts cache.
    String SCRIPTS_METADATA_CACHE_NAME = "___script_cache";
    // The name of the Protobuf definitions cache.
    String PROTOBUF_METADATA_CACHE_NAME = "___protobuf_metadata";
    // All error status keys end with this suffix. This is also the name of the global error key.
    String ERRORS_KEY_SUFFIX = ".errors";
    // All protobuf definition source files must end with this suffix.
    String PROTO_KEY_SUFFIX = ".proto";


    Logger logger = LoggerFactory.getLogger(QueriesTester.class);

    @GetMapping("/cache-manager/configuration")
    public String getCacheManagerConfiguration()  {
        return queriesCacheManager.getManager().getConfiguration().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/statistics/server")
    public String getCacheServerStatistics()  {
        return queriesCacheManager.getBookCache().serverStatistics().getStatsMap().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/statistics/client")
    public String getCacheClientStatistics()  {
        RemoteCacheClientStatisticsMXBean clientStatistics = queriesCacheManager.getBookCache().clientStatistics();

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

    @GetMapping("/cache/size")
    public String getAll()  {

        RemoteCache<String, Book> cache = queriesCacheManager.getBookCache();

        return  cache.entrySet().size() + System.lineSeparator();
    }

    @GetMapping("/cache/register/proto")
    public String cacheRegisterProto() throws Exception {

        // Register Team schema in the server
        Path protoPath = Paths.get(RemoteQuery.class.getClassLoader().getResource("proto/book.proto").toURI());
        String proto = Files.readString(protoPath);

//        logger.info("--> Proto schema: " + proto);

        RemoteCache<String, String> protoCache = remoteCacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

        if (!protoCache.containsKey("book.proto")) {
            protoCache.put("book.proto", proto);
        }

        String errors = protoCache.get(ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors:\n" + errors);
        }

        return "Protobuf cache now containes " + protoCache.entrySet().size() + " entries: " +  protoCache.entrySet().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/register/script")
    public String cacheRegisterScripts()  {

        String script = "// mode=local,language=javascript\n"
                + "var cache = cacheManager.getCache(\"default\");\n"
                + "cache.put(key, value);";

        RemoteCache<String, String> scriptCache = remoteCacheManager.getCache(SCRIPTS_METADATA_CACHE_NAME);
        if (!scriptCache.containsKey("putEntries.js")) {
            scriptCache.put("putEntries.js", script);
        }

        return  scriptCache.entrySet().size() + System.lineSeparator();
    }


    @GetMapping("/cache/load")
    public String loadBooksCache() throws Exception{

//        RemoteCache<String, Book> cache = queriesCacheManager.getBookCache();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/books.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Book book = new Book(values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.info("Book " + values[0].trim() + book.toString());
                queriesCacheManager.getBookCache().put(values[0].trim(), book);
            }
        }

//        queriesCacheManager.getBookCache().put ("100", new Book("Alvaro", "Lopez", 1993));




        return "";
    }

    @GetMapping("/cache/register/test")
    public String test()  {

        RemoteCache<String, Book> cache = queriesCacheManager.getBookCache();

        return "";
    }


}
