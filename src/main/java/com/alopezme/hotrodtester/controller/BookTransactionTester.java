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
public class BookTransactionTester {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

    @Autowired
    private RemoteCache<Integer, Book> transactionalBooksCache;

    @Autowired
    private RemoteCache<Integer, Book> transactionalBooksCache2;

    Logger logger = LoggerFactory.getLogger(BookTransactionTester.class);


    /**
     * Test 01:
     * Normal transaction with puts and gets with commit
     */
    @GetMapping("/test01/{value}")
    public String test01 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        Book trollBook = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction test 01 BEGINS");
        logger.debug("--> Transaction - New value: " + trollBook.toString());
        // Obtain the transaction manager
        TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.debug("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());
        // Perform some operations within a transaction and commit it
        transactionManager.begin();
        logger.debug("--> Transaction - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook);
        logger.debug("--> Transaction - Updated value: " + transactionalBooksCache.get(valueID));
        transactionManager.commit();
        logger.debug("--> Transaction - Final   value: " + transactionalBooksCache.get(valueID));
        logger.info("--> Transaction test 01 ENDS");

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

        logger.info("--> Transaction test 02 BEGINS");
        logger.debug("--> Transaction - New value: " + trollBook.toString());
        // Obtain the transaction manager
        TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.debug("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());
        // Perform some operations within a transaction and commit it
        transactionManager.begin();
        logger.debug("--> Transaction - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook);
        logger.debug("--> Transaction - Updated value: " + transactionalBooksCache.get(valueID));
        transactionManager.rollback();
        logger.debug("--> Transaction - Final   value: " + transactionalBooksCache.get(valueID));
        logger.info("--> Transaction test 02 ENDS");

        return transactionalBooksCache.get(valueID).toString();
    }

    /**
     * Test 03:
     * Two transactions simultaneously over the same value, rollback
     */
    @GetMapping("/test03/{value}")
    public String test03 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException, InvalidTransactionException {

        Book trollBook1 = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);
        Book trollBook2 = new Book(valueID,"Sleeping at home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction test 03 BEGINS");
        logger.debug("--> Transaction 1 - New value: " + trollBook1.toString());
        logger.debug("--> Transaction 2 - New value: " + trollBook2.toString());
        // Obtain the transaction manager
        final TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.debug("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());

        // Transaction 1 begins
        transactionManager.begin();
        logger.debug("--> Transaction 1 - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook1);
        logger.debug("--> Transaction 1 - Updated value: " + transactionalBooksCache.get(valueID));
        final Transaction tx1 = transactionManager.suspend();

        logger.debug("--> Transaction 1 - Suspended value: " + transactionalBooksCache.get(valueID));

        // Transaction 2 begins
        transactionManager.begin();
        logger.debug("--> Transaction 2 - Initial value: " + transactionalBooksCache.get(valueID));
        transactionalBooksCache.put(valueID, trollBook2);
        logger.debug("--> Transaction 2 - Updated value: " + transactionalBooksCache.get(valueID));
        transactionManager.commit();

        logger.debug("--> Transaction 2 - Committed value: " + transactionalBooksCache.get(valueID));

        // Transaction 1 resumes
        transactionManager.resume(tx1);
        //it shouldn't see the other transaction updates!
        logger.debug("--> Transaction 1 - Resumed value: " + transactionalBooksCache.get(valueID));
        transactionManager.rollback();
        logger.debug("--> Transaction 1 - Committed value: " + transactionalBooksCache.get(valueID));

        logger.info("--> Transaction test 03 ENDS");

        return transactionalBooksCache.get(valueID).toString();
    }


    /**
     * Test 04:
     * Two transactions simultaneously over different value, commit
     */
    @GetMapping("/test04/{value}")
    public String test04 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException, InvalidTransactionException {
        final int valueID1 = valueID;
        final int valueID2 = valueID + 1;
        Book trollBook1 = new Book(valueID1,"Coding from home" ,"Álvaro López Medina",2021);
        Book trollBook2 = new Book(valueID2,"Sleeping at home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction test 04 BEGINS");
        logger.debug("--> Transaction 1 - New value: " + trollBook1.toString());
        logger.debug("--> Transaction 2 - New value: " + trollBook2.toString());
        // Obtain the transaction manager
        final TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();
        logger.debug("--> Transaction - isTransactional?: " + transactionalBooksCache.isTransactional());

        // Transaction 1 begins
        transactionManager.begin();
        logger.debug("--> Transaction 1 - Initial value: " + transactionalBooksCache.get(valueID1));
        transactionalBooksCache.put(valueID1, trollBook1);
        logger.debug("--> Transaction 1 - Updated value: " + transactionalBooksCache.get(valueID1));
        final Transaction tx1 = transactionManager.suspend();

        logger.debug("--> Transaction 1 - Suspended value: " + transactionalBooksCache.get(valueID1));

        // Transaction 2 begins
        transactionManager.begin();
        logger.debug("--> Transaction 2 - Initial value: " + transactionalBooksCache.get(valueID2));
        transactionalBooksCache.put(valueID2, trollBook2);
        logger.debug("--> Transaction 2 - Updated value: " + transactionalBooksCache.get(valueID2));
        transactionManager.commit();

        logger.debug("--> Transaction 2 - Committed value: " + transactionalBooksCache.get(valueID2));

        // Transaction 1 resumes
        transactionManager.resume(tx1);
        //it shouldn't see the other transaction updates!
        logger.debug("--> Transaction 1 - Resumed value: " + transactionalBooksCache.get(valueID1));
        logger.debug("--> Transaction 2 - Resumed value: " + transactionalBooksCache.get(valueID2));
        transactionManager.commit();
        logger.debug("--> Transaction 1 - Committed value: " + transactionalBooksCache.get(valueID1));
        logger.debug("--> Transaction 2 - Committed value: " + transactionalBooksCache.get(valueID2));

        logger.info("--> Transaction test 04 ENDS");

        return transactionalBooksCache.get(valueID1).toString() + System.lineSeparator()
                + transactionalBooksCache.get(valueID2).toString();
    }




    /**
     * Test 05:
     * One transaction simultaneously over two different caches, commit
     */
    @GetMapping("/test05/{value}")
    public String test05 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException, InvalidTransactionException {

        Book trollBook1 = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);
        Book trollBook2 = new Book(valueID,"Sleeping at home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction test 05 BEGINS");
        logger.debug("--> Transaction - New value for cache 1: " + trollBook1.toString());
        logger.debug("--> Transaction - New value for cache 2: " + trollBook2.toString());

        // Obtain the transaction manager
        // The transaction Manager is common to all the caches (In fact should be common to all the app)
        final TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();

        logger.debug("--> Cache 1 - isTransactional?: " + transactionalBooksCache.isTransactional());
        logger.debug("--> Cache 2 - isTransactional?: " + transactionalBooksCache2.isTransactional());

        // Transaction begins
        transactionManager.begin();
        logger.debug("--> Transaction - Cache 1 - Initial value: " + transactionalBooksCache.get(valueID));
        logger.debug("--> Transaction - Cache 2 - Initial value: " + transactionalBooksCache2.get(valueID));

        transactionalBooksCache.put(valueID, trollBook1);
        transactionalBooksCache2.put(valueID, trollBook2);

        logger.debug("--> Transaction - Cache 1 - Updated value: " + transactionalBooksCache.get(valueID));
        logger.debug("--> Transaction - Cache 2 - Updated value: " + transactionalBooksCache2.get(valueID));

        transactionManager.commit();

        logger.info("--> Transaction test 05 ENDS");

        logger.debug("--> Transaction - Cache 1 - Committed value: " + transactionalBooksCache.get(valueID));
        logger.debug("--> Transaction - Cache 2 - Committed value: " + transactionalBooksCache2.get(valueID));

        return transactionalBooksCache.get(valueID).toString() + System.lineSeparator()
                + transactionalBooksCache.get(valueID).toString();
    }


    /**
     * Test 06:
     * One transaction simultaneously over two different caches, rollback
     */
    @GetMapping("/test06/{value}")
    public String test06 (
            @PathVariable(value = "value") int valueID) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException, InvalidTransactionException {

        Book trollBook1 = new Book(valueID,"Coding from home" ,"Álvaro López Medina",2021);
        Book trollBook2 = new Book(valueID,"Sleeping at home" ,"Álvaro López Medina",2021);

        logger.info("--> Transaction test 06 BEGINS");
        logger.debug("--> Transaction - New value for cache 1: " + trollBook1.toString());
        logger.debug("--> Transaction - New value for cache 2: " + trollBook2.toString());

        // Obtain the transaction manager
        // The transaction Manager is common to all the caches (In fact should be common to all the app)
        final TransactionManager transactionManager = transactionalBooksCache.getTransactionManager();

        logger.debug("--> Cache 1 - isTransactional?: " + transactionalBooksCache.isTransactional());
        logger.debug("--> Cache 2 - isTransactional?: " + transactionalBooksCache2.isTransactional());

        // Transaction begins
        transactionManager.begin();
        logger.debug("--> Transaction - Cache 1 - Initial value: " + transactionalBooksCache.get(valueID));
        logger.debug("--> Transaction - Cache 2 - Initial value: " + transactionalBooksCache2.get(valueID));

        transactionalBooksCache.put(valueID, trollBook1);
        transactionalBooksCache2.put(valueID, trollBook2);

        logger.debug("--> Transaction - Cache 1 - Updated value: " + transactionalBooksCache.get(valueID));
        logger.debug("--> Transaction - Cache 2 - Updated value: " + transactionalBooksCache2.get(valueID));

        transactionManager.rollback();

        logger.info("--> Transaction test 06 ENDS");

        logger.debug("--> Transaction - Cache 1 - Committed value: " + transactionalBooksCache.get(valueID));
        logger.debug("--> Transaction - Cache 2 - Committed value: " + transactionalBooksCache2.get(valueID));

        return "Finished";
    }



    /**
     * Test 07:
     * Check that the Transaction Manager is common to all the caches
     */
    @GetMapping("/test07")
    public String test07 () {

        // Obtain the transaction manager
        final TransactionManager transactionManager1 = transactionalBooksCache.getTransactionManager();
        final TransactionManager transactionManager2 = transactionalBooksCache2.getTransactionManager();

        logger.info("--> transactionManager1 to String: " + transactionManager1.toString());
        logger.info("--> transactionManager2 to String: " + transactionManager2.toString());
        logger.info("--> transactionManager1 Hash Code: " + System.identityHashCode(transactionManager1));
        logger.info("--> transactionManager2 Hash Code: " + System.identityHashCode(transactionManager2));

        return "END";
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
