package com.tongji.boying.service.impl;

import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.model.Ticket;
import com.tongji.boying.service.UserService;
import com.tongji.boying.service.UserTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTicketServiceImpl implements UserTicketService {
    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public int add(int orderId, int showClassId) {
        String sql = "select max(ticket_id) from ticket";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        if (count == null) {
            count = 1;
        }
        sql = "INSERT INTO ticket VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, count + 1, orderId, showClassId, "https://tongji4m3.oss-cn-beijing.aliyuncs.com/1608898790.jpg");
    }

    @Override
    public List<Ticket> list(int orderId, Integer pageSize, Integer pageNum) {
        String sql = "select * from ticket where order_id = ?";
        try {
            return jdbcTemplate.query(sql, (resultSet, i) -> {
                Ticket ticket = new Ticket();
                ticket.setTicketId(resultSet.getInt("ticket_id"));
                ticket.setShowClassId(resultSet.getInt("show_class_id"));
                ticket.setOrderId(resultSet.getInt("order_id"));
                ticket.setQrCodeUrl(resultSet.getString("qr_code_url"));
                return ticket;
            }, orderId);
        }
        catch (Exception e) {
            Asserts.fail("要查询的票不存在");
        }
        return null;
    }

    @Override
    public Ticket getItem(int id) {
        String sql = "select * from ticket where ticket_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                Ticket ticket = new Ticket();
                ticket.setShowClassId(resultSet.getInt("show_class_id"));
                ticket.setOrderId(resultSet.getInt("order_id"));
                ticket.setTicketId(resultSet.getInt("ticket_id"));
                ticket.setQrCodeUrl(resultSet.getString("qr_code_url"));
                return ticket;
            }, id);
        }
        catch (Exception e) {
            Asserts.fail("要查询的票不存在");
        }
        return null;
    }
}
