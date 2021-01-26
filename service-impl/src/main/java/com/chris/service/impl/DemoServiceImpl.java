package com.chris.service.impl;

import com.chris.dao.IDemoDao;
import com.chris.service.api.IDemoService;
import com.chris.service.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@Named
public class DemoServiceImpl implements IDemoService {


    @Autowired
    IDemoDao demoDao;

    @Override
    public int demo() {
        String s = "123";
        Object o = StringUtil.of(s);
        return demoDao.demo();
    }
}
