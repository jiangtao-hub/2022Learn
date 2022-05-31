package com.example.servicea.controller;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Classname FeignClient
 * @Description TODO
 * @Date 2022/2/14 13:33
 * @Created by jt
 * @projectName
 */
@org.springframework.cloud.openfeign.FeignClient(name = "SERVICE-OBJCAT-B",
        url = "http://localhost:8083/service-b/service-objcat-b",fallback = FallbackFactory.Default.class)
public interface FeignClient {
    @RequestMapping("testB")
    public String TestAController();
}
