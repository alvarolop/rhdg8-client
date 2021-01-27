package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.utils.QueriesCacheManager;
import org.infinispan.client.hotrod.RemoteCache;
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
    QueriesCacheManager queriesCacheManager;

    Logger logger = LoggerFactory.getLogger(QueriesTester.class);

    @GetMapping("test")
    public String loadBooksCacheTest() throws Exception{

        RemoteCache<Integer, Book> cache = queriesCacheManager.getBookCache();

        logger.info("\n--> Test begins <--\n");
        logger.info("Content of entry #100: " + cache.get(100) + "\n");

        // This step will work correctly, as entry #100 is empty
        logger.info("Put entry #100");
        cache.put (100, new Book(100, "Alvaro", "Lopez", 1993));
        logger.info("Content of entry #100: " + cache.get(100) + "\n");

        // This section will not work, as entry #100 is already there.
        // We will retrieve the new value, but we will obtain many errors on the client side
        logger.info("Put entry #100");
        cache.put (100, new Book(100, "Alvaro", "Medina", 1993));
        logger.info("Content of entry #100: " + cache.get(100) + "\n");


        return "";
    }



}
