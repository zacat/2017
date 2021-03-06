package com.slj.api.controller;

import com.google.common.collect.ImmutableMap;
import com.slj.crawler.LoupanService;
import com.slj.crawler.anjuke.processor.AnjukePageProcessor;
import com.slj.crawler.dao.LoupanDao;
import com.slj.crawler.entity.Loupan;
import com.slj.crawler.lianjia.processor.LianjiaPageProcessor;
import com.slj.domain.LoupanChange;
import com.slj.zk.ZkConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by slj on 17/1/2.
 */
@RestController
@RequestMapping(value = "/",method = RequestMethod.GET)
public class IndexController {



    @Autowired
    private LianjiaPageProcessor lianjiaPageProcessor;

    @Autowired
    private AnjukePageProcessor anjukePageProcessor;

    @Autowired
    private LoupanService loupanService;

    @Autowired
    private LoupanDao loupanDao;

    @RequestMapping(value = "",method = RequestMethod.GET)
    public String index(){
        return "hello world";
    }


    @RequestMapping(value = "/zk",method = RequestMethod.GET)
    public String testZK (){
        ZkConfig.createOrUpdate("/test/t","hello world zk");
       String result = ZkConfig.get("/test/t");
       return result;
    }
    @RequestMapping(value = "/crawler",method = RequestMethod.GET)
    public String crawLianjia(@RequestParam("cityName") String cityName){
        lianjiaPageProcessor.discoverAll(cityName);
        return "SUCCESS";
    }
    @RequestMapping(value = "/anjuku/crawler",method = RequestMethod.GET)
    public String crawAnjuke(@RequestParam("cityName") String cityName){
        anjukePageProcessor.discoverAll(cityName);
        return "SUCCESS";
    }


    @RequestMapping(value = "/average",method = RequestMethod.GET)
    public String average(@RequestParam(defaultValue = "xa") String cityName){
        return "今日均价="+ loupanService.average(cityName,new Date()).toString();
    }

    @RequestMapping(value = "/change",method = RequestMethod.GET)
    public String newChange(@RequestParam(defaultValue = "xa") String cityName){
       List<Pair<Loupan,Loupan>> changes = loupanService.change(cityName,new Date());
       StringBuilder result = new StringBuilder("楼盘变化信息  \t\n");
        changes.stream().forEach(e->{
            result.append("  楼盘: ").append(e.getRight().getName()).append("     昨日价格:").append(e.getLeft().getPrice()).append("      今日价格:").append(e.getRight().getPrice()).append(" \t\n          ");
        });
        return result.toString();
    }
    @RequestMapping(value = "/index")
    public Map<String,Object> index(@RequestParam(defaultValue = "xa") String cityName,@RequestParam(defaultValue = "0") long monthStartDate ,@RequestParam(defaultValue = "0") long dayStartDate){
        return ImmutableMap.<String,Object>builder().put("average",loupanService.average(cityName,new Date()))
                .put("change",loupanService.change(cityName,new Date()).stream().map(e->new LoupanChange(e.getLeft(),e.getRight())).collect(Collectors.toList()))
                .put("month",loupanDao.queryMonthAverage(monthStartDate==0?new DateTime().minusYears(1).toDate():new Date(monthStartDate),cityName))
                .put("day",loupanDao.queryDayAverage(dayStartDate==0?new DateTime().minusYears(1).toDate():new Date(dayStartDate),cityName))
                .build();
    }
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String testString(@RequestParam String test){
        return test;
    }
}
