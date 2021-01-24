package com.alopezme.datagridtester.controller;

import java.util.List;

import javax.validation.Valid;

import com.alopezme.datagrid_tester.api.AdminApi;
import com.alopezme.datagrid_tester.model.AdminOperation;
import com.alopezme.datagrid_tester.model.CacheInfo;
import com.alopezme.datagrid_tester.api.CacheApi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController implements CacheApi {

    @Override
    public ResponseEntity<List<String>> getAllcaches() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<CacheInfo> getCacheInfo(String cacheName) {
        // TODO Auto-generated method stub
        return null;
    }

    
    
}
