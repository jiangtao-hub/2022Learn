package com.example.serviceb.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname TestController
 * @Description TODO
 * @Date 2022/2/14 11:28
 * @Created by jt
 * @projectName
 */
@RestController
@RequestMapping("/service-objcat-b")
public class TestController {
    @RequestMapping("testB")
    public String TestAController(){
        return "Hello,SpringCloud for TestB";
    }
}
