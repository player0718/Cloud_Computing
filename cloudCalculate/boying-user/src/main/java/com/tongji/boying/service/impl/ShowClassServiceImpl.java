package com.tongji.boying.service.impl;

import com.github.pagehelper.PageHelper;
import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.model.ShowClass;
import com.tongji.boying.model.ShowSession;
import com.tongji.boying.service.ShowClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ShowClassServiceImpl implements ShowClassService {
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public ShowClass detail(Integer classId) {
        String sql = "select * from show_class where show_class_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                ShowClass showClass = new ShowClass();
                showClass.setShowClassId(resultSet.getInt("show_class_id"));
                showClass.setShowSessionId(resultSet.getInt("show_session_id"));
                showClass.setCapacity(resultSet.getInt("capacity"));
                showClass.setPrice(resultSet.getDouble("price"));
                showClass.setName(resultSet.getString("name"));
                return showClass;
            }, classId);
        }
        catch (Exception e) {
            Asserts.fail("要查询的演出座次不存在");
        }
        return null;
    }

    @Override
    public List<ShowClass> getShowClassList(int sessionId, Integer pageNum, Integer pageSize) {

        String sql = "select * from show_class where show_session_id = ?";
        try {
            return jdbcTemplate.query(sql, (resultSet, i) -> {
                ShowClass showClass = new ShowClass();
                showClass.setShowClassId(resultSet.getInt("show_class_id"));
                showClass.setShowSessionId(resultSet.getInt("show_session_id"));
                showClass.setCapacity(resultSet.getInt("capacity"));
                showClass.setPrice(resultSet.getDouble("price"));
                showClass.setName(resultSet.getString("name"));
                return showClass;
            }, sessionId);
        }
        catch (Exception e) {
            Asserts.fail("要查询的演出座次不存在");
        }
        return null;
    }
}
