package org.alopezme.example.hotrodtester.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Test {

    @GetMapping("/health")
    public String health() {
        return "Works!!";
    }
}
