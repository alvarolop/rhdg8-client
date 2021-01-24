package com.alopezme.datagridtester.controller;

import javax.validation.Valid;

import com.alopezme.datagrid_tester.api.AdminApi;
import com.alopezme.datagrid_tester.model.AdminOperation;
import com.alopezme.datagridtester.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController implements AdminApi {

    @Autowired
    AdminService adminService;

    @Override
    public ResponseEntity<Void> resetCacheManager(@Valid AdminOperation adminOperation) {

        adminService.reset();

        
        return null;
    }
    
}
