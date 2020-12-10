package org.alopezme.example.spring;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("api")
public class HotRodTester {

    @Autowired
    RemoteCacheManager rcm;

    Logger logger = LoggerFactory.getLogger(HotRodTester.class);






    @GetMapping("/reset")
    public String reset() {

        rcm.stop();
        rcm.start();

        return "SpringCache Manager restarted" + System.lineSeparator();
    }


    @GetMapping("/cache")
    public String caches() {

        return rcm.getCacheNames().toString() + System.lineSeparator();
    }


    @GetMapping("/cache/{cache}/stats")
    public String stats(
            @PathVariable(value = "cache") String cacheName) {

        return rcm.getCache(cacheName).serverStatistics().getStatsMap().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/create")
    public String create(
            @PathVariable(value = "cache") String cacheName) {
/*
        Configuration config = new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.DIST_ASYNC)
                .memory()
                    .size(20000)
                .expiration()
                    .wakeUpInterval(5000L)
                    .maxIdle(120000L)
                .build();

        //rcm.administration().getOrCreateCache(cacheName, config);
        rcm.administration().getOrCreateCache(cacheName, new XMLStringConfiguration(config.toXMLString()));
*/
        return "ok" + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/put")
    public String put(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "async", defaultValue = "false") boolean isAsync,
            @RequestParam(value = "asyncTime", defaultValue = "500") int asyncTime,
            @RequestParam(value = "size", defaultValue = "1024") int entrySize,
            @RequestParam(value = "minkey", defaultValue = "0") int minKey,
            @RequestParam(value = "keyrange", required = false) Integer entryKeyRange) {

        RemoteCache<String, byte[]> cache = rcm.getCache(cacheName);

        int keyrange = numEntries;
        if (entryKeyRange != null)
            keyrange = entryKeyRange;

        byte[] bytes = new byte[entrySize];
        Random rnd = new Random();

        int key = 0;

        for (int i = minKey; i < (minKey + numEntries); i++) {

            rnd.nextBytes(bytes);

            try {
                if (isAsync) {
                    cache.put(Integer.toString(key + minKey), bytes, asyncTime, TimeUnit.MILLISECONDS);
                    logger.info("put ok " + i + " Async");
                } else {
                    cache.put(Integer.toString(key + minKey), bytes);
                    logger.info("put ok " + i);
                }
            } catch (Exception e) {
                logger.error("Exception in put " + i, e);
            }

            key++;
            key %= keyrange;
        }

        return "OK " + numEntries + " " + entrySize + " " + minKey + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/put-simple")
    public String putSimple(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "minkey", defaultValue = "0") int minKey,
            @RequestParam(value = "entrycontent", defaultValue = "Test") String entryContent,
            @RequestParam(value = "keyrange", required = false) Integer entryKeyRange) {

        RemoteCache<String, String> cache = rcm.getCache(cacheName);

        int keyrange = numEntries;
        if (entryKeyRange != null)
            keyrange = entryKeyRange;

        int key = 0;

        for (int i = minKey; i < (minKey + numEntries); i++) {

            try {
                cache.put(Integer.toString(key + minKey), entryContent + " " + Integer.toString(key + minKey));
                System.out.println("put ok " + i);
            } catch (Exception e) {
                System.out.println("Exception in put " + i + e);
            }

            key++;
            key %= keyrange;
        }

        return "OK " + numEntries + " " + minKey + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/get")
    public String get(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "async", defaultValue = "false") boolean isAsync,
            @RequestParam(value = "asyncTime", defaultValue = "500") int asyncTime,
            @RequestParam(value = "minkey", defaultValue = "0") int minKey) {

        RemoteCache<String, byte[]> cache = rcm.getCache(cacheName);

        for (int i = minKey; i < (minKey + numEntries); i++) {
            if (isAsync) {
                try {
                    cache.getAsync(Integer.toString(i)).get(asyncTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.error("Exception in put async " + i, e);
                    e.printStackTrace();
                }
                logger.info("get ok " + i + " Async");
            } else {
                cache.get(Integer.toString(i));
                logger.info("get ok " + i);
            }

        }

        return "OK " + numEntries + " " + minKey + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/get-single")
    public String getSingle(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "key") int key) {

        RemoteCache<String, byte[]> cache = rcm.getCache(cacheName);

        return Arrays.toString(cache.get(Integer.toString(key))) + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/get-single-string")
    public String getSingleString(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "key") int key) {

        RemoteCache<String, String> cache = rcm.getCache(cacheName);

        return cache.get(Integer.toString(key)) + System.lineSeparator();
    }


    @GetMapping("/cache/{cache}/get-keys")
    public String getKeys(
            @PathVariable(value = "cache") String cacheName) {

        return rcm.getCache(cacheName).keySet().toString() + System.lineSeparator();
    }

    @GetMapping("/cache/{cache}/remove")
    public String remove(
            @PathVariable(value = "cache") String cacheName,
            @RequestParam(value = "entries") int numEntries,
            @RequestParam(value = "minkey", required = false) Integer entryMinkey) {

        RemoteCache<String, byte[]> cache = rcm.getCache(cacheName);

        int min = 0;
        if (entryMinkey != null)
            min = entryMinkey;

        for (int i = min; i < (min + numEntries); i++) {
            cache.remove(Integer.toString(i));
        }

        return "OK " + numEntries + " " + entryMinkey + System.lineSeparator();
    }

}