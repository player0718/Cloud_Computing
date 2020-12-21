package com.tongji.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hive2")
public class HiveController {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Map<String, Object>> list() {
        String sql = "select * from pokes";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list;
    }

}

