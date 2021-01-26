package com.chris.service.impl;

import com.chris.dao.IDemoDao;
import com.chris.service.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Author: shaoqing
 * date-time: 2021-01-24 20:09
 **/
//@SpringBootTest(classes = DemoServiceImpl.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({StringUtil.class})
//@PowerMockIgnore("javax.management.*")
public class DemoServiceImplTest {

    @Mock
    IDemoDao demoDao;

    @InjectMocks
    DemoServiceImpl demoServiceImpl;


    @Test
    public void demo() {
        mockStatic(StringUtil.class);
        when(StringUtil.of(Mockito.anyString())).thenReturn("123");
        Mockito.doReturn(123).when(demoDao).demo();
        int demo = demoServiceImpl.demo();
        System.out.println(demo);
    }


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


}