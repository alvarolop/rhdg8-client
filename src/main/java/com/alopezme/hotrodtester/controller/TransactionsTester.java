package com.alopezme.hotrodtester.controller;

import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/transaction")
public class TransactionsTester {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

    Logger logger = LoggerFactory.getLogger(TransactionsTester.class);


}
