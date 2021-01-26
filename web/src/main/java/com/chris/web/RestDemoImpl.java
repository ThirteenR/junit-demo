package com.chris.web;

import com.chris.service.api.IDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Author: shaoqing
 * date-time: 2021-01-24 19:30
 **/
@RestController
public class RestDemoImpl {

    @Inject
    IDemoService demoServiceImpl;

    @RequestMapping("/demo")
    public String Demo() {
        return String.valueOf(demoServiceImpl.demo());
    }
}
