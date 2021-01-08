package org.alopezme.example.springtester.controller;

import org.alopezme.example.springtester.queries.QueriesCacheManager;
import org.alopezme.example.springtester.model.Book;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.impl.query.RemoteQuery;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
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
import java.util.*;
import java.util.stream.Collectors;

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






    /***
     * STATISTICS AND CONFIGURATION
     */

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






    /***
     * REGISTER PROTOS AND SCRIPTS
     */

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

        return "Protobuf cache now contains " + protoCache.entrySet().size() + " entries: " + System.lineSeparator() +
                protoCache.entrySet().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/register/script")
    public String cacheRegisterScripts()  {

        String script = "// mode=local,language=javascript\n"
                + "var cache = cacheManager.getCache(cacheName);\n"
                + "cache.put(key, value);";

        RemoteCache<String, String> scriptCache = remoteCacheManager.getCache(SCRIPTS_METADATA_CACHE_NAME);
        if (!scriptCache.containsKey("putEntries.js")) {
            scriptCache.put("putEntries.js", script);
        }

        return "Scripts cache now contains " + scriptCache.entrySet().size() + " entries: " + System.lineSeparator() +
                scriptCache.entrySet().toString() + System.lineSeparator();

    }






    /***
     * MANAGE CACHE: SIZE, LOAD, ETC.
     */

    @GetMapping("/cache/book/size")
    public String getAll()  {

        RemoteCache<Integer, Book> cache = queriesCacheManager.getBookCache();

        return  cache.entrySet().size() + System.lineSeparator();
    }


    @GetMapping("/cache/book/load")
    public String loadBooksCache() throws Exception{

        RemoteCache<Integer, Book> cache = queriesCacheManager.getBookCache();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/books.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                if (!cache.containsKey(book.getId())){
                    logger.info(book.toTextString());
                    cache.put(book.getId(), book);
                }
            }
        }
        return "Books cache now contains " + cache.entrySet().size() + " entries";
    }

    @GetMapping("/cache/book/test-load")
    public String loadBooksCacheTest() throws Exception{

        RemoteCache<Integer, Book> cache = queriesCacheManager.getBookCache();
        if (!cache.containsKey("100")){
            cache.put (100, new Book(100, "Alvaro", "Lopez", 1993));
        }

        return "";
    }

    @GetMapping("/cache/book/rebuild-index")
    public String rebuildIndex(){

        queriesCacheManager.getManager().administration().reindexCache(queriesCacheManager.getBookCache().getName());

        return "Reindexing...this task will be performed async";
    }








    /***
     * QUERIES: GET
     */

    @GetMapping("/cache/book/query/title")
    public String queryByTitle()  {

        QueryFactory queryFactory = Search.getQueryFactory(queriesCacheManager.getBookCache());
        Query<Book> query = queryFactory.create("FROM org.alopezme.springtester.Book WHERE title='The Iliad'");
        List<Book> list = query.execute().list();

        return list.toString();
    }

    @GetMapping("/cache/book/query/author")
    public String queryByAuthor()  {

        QueryFactory queryFactory = Search.getQueryFactory(queriesCacheManager.getBookCache());
        Query<Book> query = queryFactory.create("FROM org.alopezme.springtester.Book WHERE author:'Homer'");
        List<Book> list = query.execute().list();

        return list.toString();
    }







    /***
     * QUERIES: REMOVE
     */

    @GetMapping("/cache/book/query/remove-01")
    public String queryRemove01()  {

        QueryFactory queryFactory = Search.getQueryFactory(queriesCacheManager.getBookCache());
        Query<Book> query = queryFactory.create("FROM org.alopezme.springtester.Book WHERE title='The Iliad'");
        List<Book> list = query.execute().list(); // Voila! We have our book back from the cache!
        logger.info("Removing ... " + list.toString());
        for (Book book : list ) {
//            queriesCacheManager.getBookCache().remove(book.getId());
        }
        return list.toString();
    }

    @GetMapping("/cache/book/query/remove-02")
    public String queryRemove02()  {

        QueryFactory queryFactory = Search.getQueryFactory(queriesCacheManager.getBookCache());
        Query<Object[]> query = queryFactory.create("SELECT id FROM org.alopezme.springtester.Book WHERE author:'Homer'");
        List<Object[]> list = query.execute().list();
        List<Integer> result = new ArrayList<Integer>();
        for (Object[] book : list ) {
            logger.info("Removing book " + Integer.toString((Integer)book[0]));
//            queriesCacheManager.getBookCache().remove(book[0]);
            result.add((Integer)book[0]);
        }
        return result.toString();
    }

    @GetMapping("/cache/book/query/remove-03")
    public String queryRemove03()  {

        QueryFactory queryFactory = Search.getQueryFactory(queriesCacheManager.getBookCache());
        Query<Object[]> query = queryFactory.create("SELECT id FROM org.alopezme.springtester.Book WHERE author:'Homer'");
        List<Object[]> list = query.execute().list();
        Set<Integer> listToRemove = list.stream()
                .map(row -> (Integer) row[0])
                .collect(Collectors.toSet());
//        queriesCacheManager.getBookCache().keySet().removeAll(listToRemove);

        return listToRemove.toString();
    }








    /***
     * SCRIPTS: EXECUTE
     */
    @GetMapping("/cache/book/script/execute")
    public String executeScript()  {

        Map<String, Object> params = new HashMap<>();
        params.put("key", "myKey");
        params.put("value", "myValue");
        params.put("cacheName", queriesCacheManager.getCacheName());

        logger.info("-------> Get \"myKey\": " + queriesCacheManager.getBookCache().get("myKey"));
        Object result = queriesCacheManager.getBookCache().execute("putEntries.js", params);
        logger.info("-------> Get \"myKey\": " + queriesCacheManager.getBookCache().get("myKey"));

        return "Script executed";
    }

}
