package com.alopezme.datagridtester.service.impl;

import com.alopezme.datagridtester.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;

@Service
public class AdminServiceimpl implements AdminService {

    // @Autowired
    // RemoteCacheManager remoteCacheManager;

    @Autowired
    SpringRemoteCacheManager springRemoteCacheManager;

    @Override
    public void reset() {

        springRemoteCacheManager.stop();
        springRemoteCacheManager.start();

    }

}
