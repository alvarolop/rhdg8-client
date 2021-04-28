package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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




}
