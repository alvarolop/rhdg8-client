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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TransactionsTester showcases examples of how to use transactional operations on DG caches.
 * There are some links that should be taken into consideration to learn about Transactions on DH:
 * - https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/hot_rod_java_client_guide/index#hotrod_transactions
 * - https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/data_grid_developer_guide/index#transaction_manager
 *
 * The methods included in this class to test DG Transactions are inspired in the real tests used by the engineering team of Infinispan:
 * - https://github.com/infinispan/infinispan/blob/master/client/hotrod-client/src/test/java/org/infinispan/client/hotrod/tx/TxFunctionalTest.java
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
     * Test 01:
     * Normal transaction with puts and gets with commit
     */
    @GetMapping("/test01/{value}")
    public String test01 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        Book trollBook = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction - New value: " + trollBook.toString());

        logger.debug("--> Transaction BEGINS");
        // Obtain the transaction manager
        TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.debug("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());
        // Perform some operations within a transaction and commit it
        transactionManager.begin();
        logger.debug("--> Transaction - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook);
        logger.debug("--> Transaction - Updated value: " + transactionalBooksCache.get(valueID));
        transactionManager.commit();
        logger.debug("--> Transaction ENDS");
        logger.debug("--> Transaction - Final   value: " + transactionalBooksCache.get(valueID));

        return transactionalBooksCache.get(valueID).toString();
    }

    /**
     * Test 02:
     * Normal transaction with puts and gets with rollback
     */
    @GetMapping("/test02/{value}")
    public String test02 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        Book trollBook = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction - New value: " + trollBook.toString());

        logger.debug("--> Transaction BEGINS");
        // Obtain the transaction manager
        TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.debug("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());
        // Perform some operations within a transaction and commit it
        transactionManager.begin();
        logger.debug("--> Transaction - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook);
        logger.debug("--> Transaction - Updated value: " + transactionalBooksCache.get(valueID));
        transactionManager.rollback();
        logger.debug("--> Transaction ENDS");
        logger.debug("--> Transaction - Final   value: " + transactionalBooksCache.get(valueID));

        return transactionalBooksCache.get(valueID).toString();
    }







    @GetMapping("/reduced-load")
    public String reducedLoadBooksCache() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/books.csv")))) {
            String line;
            int iteration = 0;
            while ((line = br.readLine()) != null && iteration < 100) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.debug("PUT : " + book.toString());
                transactionalBooksCache.put(book.getId(), book);
                iteration++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Books cache now contains " + transactionalBooksCache.size() + " entries";
    }

}
