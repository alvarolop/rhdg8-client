package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("tester")
public class TesterController {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

    @Autowired
    private RemoteCache<String, Byte[]> byteTesterCache;

    @Autowired
    private RemoteCache<String, String> stringTesterCache;

    Logger logger = LoggerFactory.getLogger(TesterController.class);

    /**
     * GET KEYS
     */
    @GetMapping("/cache/{cache}/keys")
    public String getKeys(
            @PathVariable(value = "cache") String cacheName) {

        return byteTesterCache.keySet().toString() + System.lineSeparator();
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
}
