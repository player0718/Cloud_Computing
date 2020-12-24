package com.tongji.boying.service.impl;

import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.model.ShowSession;
import com.tongji.boying.service.ShowSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ShowSessionServiceImpl implements ShowSessionService
{
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public ShowSession detail(Integer sessionId)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String sql = "select * from show_session where weight != 0 and show_session_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                ShowSession showSession = new ShowSession();
                showSession.setShowSessionId(resultSet.getInt("show_session_id"));
                showSession.setShowId(resultSet.getInt("show_id"));
                showSession.setWeight(resultSet.getInt("weight"));
                try {
                    showSession.setStartTime(format.parse(resultSet.getString("start_time")));
                    showSession.setEndTime(format.parse(resultSet.getString("end_time")));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return showSession;
            }, sessionId);
        }
        catch (Exception e) {
            Asserts.fail("要查询的演出场次不存在");
        }
        return null;
    }

    @Override
    public List<ShowSession> getShowSessionList(int id, Integer pageNum, Integer pageSize)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String sql = "select * from show_session where weight != 0 and show_id = ?";
        try {
            return jdbcTemplate.query(sql, (resultSet, i) -> {
                ShowSession showSession = new ShowSession();
                showSession.setShowSessionId(resultSet.getInt("show_session_id"));
                showSession.setShowId(resultSet.getInt("show_id"));
                showSession.setWeight(resultSet.getInt("weight"));
                try {
                    showSession.setStartTime(format.parse(resultSet.getString("start_time")));
                    showSession.setEndTime(format.parse(resultSet.getString("end_time")));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return showSession;
            }, id);
        }
        catch (Exception e) {
            Asserts.fail("要查询的演出场次不存在");
        }
        return null;
    }
}
