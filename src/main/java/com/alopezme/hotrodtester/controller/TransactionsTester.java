package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.*;

/**
 * TransactionsTester showcases examples of how to use transactional operations on DG caches.
 * There are some links that should be taken into consideration to learn about Transactions on DH:
 * - https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/hot_rod_java_client_guide/index#hotrod_transactions
 * - https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/data_grid_developer_guide/index#transaction_manager
 */

@RestController
@RequestMapping("transaction")
public class TransactionsTester {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

    @Autowired
    private RemoteCache<Integer, Book> transactionalBooksCache;


    Logger logger = LoggerFactory.getLogger(TransactionsTester.class);


    /**
     * Testing
     */
    @GetMapping("/update/{value}")
    public String updateValueTransactionally (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        Book trollBook = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction - New value: " + trollBook.toString());

        logger.info("--> Transaction BEGINS");
        // Obtain the transaction manager
        TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.info("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());
        // Perform some operations within a transaction and commit it
        transactionManager.begin();
        logger.info("--> Transaction - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook);
        logger.info("--> Transaction - Updated value: " + transactionalBooksCache.get(valueID));
        transactionManager.commit();
        logger.info("--> Transaction ENDS");
        logger.info("--> Transaction - Final   value: " + transactionalBooksCache.get(valueID));


//        // Display the current cache contents
//        System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
//        // Perform some operations within a transaction and roll it back
//        transactionManager.begin();
//        cache.put("key1", "value3");
//        cache.put("key2", "value4");
//        transactionManager.rollback();
//        // Display the current cache contents
//        System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
//        // Stop the cache manager and release all resources
//        cacheManager.stop();

        return transactionalBooksCache.get(valueID).toString();
    }

}
