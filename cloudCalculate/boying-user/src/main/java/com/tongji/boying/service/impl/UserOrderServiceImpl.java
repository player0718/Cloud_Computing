package com.tongji.boying.service.impl;

import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.dto.UserOrderParam;
import com.tongji.boying.model.ShowClass;
import com.tongji.boying.model.ShowSession;
import com.tongji.boying.model.User;
import com.tongji.boying.model.UserOrder;
import com.tongji.boying.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserOrderServiceImpl implements UserOrderService {
    @Autowired
    private UserService userService;
    @Autowired
    private ShowClassService showClassService;
    @Autowired
    private ShowSessionService showSessionService;
    @Autowired
    private UserTicketService userTicketService;
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public int add(UserOrderParam param) {
        Integer showSessionId = param.getShowSessionId();
        List<Integer> showClassIds = param.getShowClassIds();
        User user = userService.getCurrentUser();

        String sql = "select count(*) from user_order where user_id = ? and show_session_id = ? and status != 3";
        Integer dbOrderCount = jdbcTemplate.queryForObject(sql, Integer.class, user.getUserId(), showSessionId);

        if (dbOrderCount != null && dbOrderCount != 0) {
            Asserts.fail("不能反复对同一个场次下单！");
        }

        if (showClassIds.size() == 0) {
            Asserts.fail("一个订单至少要有1张票!");
        }
        if (showClassIds.size() > 5) {
            Asserts.fail("一个订单最多只能有5张票!");
        }

        ShowSession showSession = showSessionService.detail(showSessionId);
        if (showSession == null) {
            Asserts.fail("演出场次选择不合法!");
        }
        //数据库中的座次信息
        List<ShowClass> dbShowClasses = showClassService.getShowClassList(showSession.getShowSessionId(), 1, 5);
        List<Integer> dbShowClassIds = dbShowClasses.stream()
                .map(ShowClass::getShowClassId)
                .collect(Collectors.toList());
        //校验座次是否合法
        for (Integer showClassId : showClassIds) {
            if (!dbShowClassIds.contains(showClassId)) {
                Asserts.fail("演出座次选择不合法!");
            }
        }

        //生成订单
        UserOrder order = new UserOrder();
        order.setUserId(user.getUserId());
        order.setShowSessionId(showSessionId);
        order.setAddressId(null);//0约定为不邮寄
        order.setFrequentId(user.getUserId());
        order.setStatus(1);//待观看状态
        order.setTime(new Date());
        order.setPayment("支付宝");
        order.setUserDelete(false);
        order.setShowId(showSession.getShowId());


        //订单总数,订单总金额
        double totalMoney = 0;
        int count = 0;

        for (Integer showClassId : showClassIds) {
            ShowClass showClass = showClassService.detail(showClassId);


            totalMoney += showClass.getPrice();
            ++count;
        }
        order.setTicketCount(count);
        order.setMoney(totalMoney);

        sql = "select max(order_id) from user_order";
        Integer orderCount = jdbcTemplate.queryForObject(sql, Integer.class);
        if (orderCount == null) {
            orderCount = 1;
        }
        sql = "insert into user_order values(?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, orderCount + 1, user.getUserId(), showSession.getShowId(), showSessionId, "1", new Date(), "支付宝", "0", totalMoney, count);
        sql = "select max(order_id) from user_order";
        int insert = jdbcTemplate.queryForObject(sql, Integer.class);
        //生成票
        for (Integer showClassId : showClassIds) {
            userTicketService.add(insert, showClassId);
        }

        return insert;
    }

    @Override
    public int delete(int id) {
        // TODO: 2020/12/26 修改删除不被hive允许
        User user = userService.getCurrentUser();
        String sql = "update user_order set user_delete = ? where user_id = ? and order_id = ?";
        return jdbcTemplate.update(sql, "1", user.getUserId(), id);
    }

    @Override
    public int cancel(int id) {
        User user = userService.getCurrentUser();
        String sql = "update user_order set status = ? where user_id = ? and order_id = ?";
        return jdbcTemplate.update(sql, "3", user.getUserId(), id);
    }

    @Override
    public List<UserOrder> list(Integer status, Integer pageNum, Integer pageSize) {
        if (status == null || status == -1) {
            //默认查询未观看订单
            status = 1;
        }
        User user = userService.getCurrentUser();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //已退票的不算
        String sql = "select * from user_order where user_id = ?  and status = ? and user_delete = ?";
        try {
            return jdbcTemplate.query(sql, (resultSet, i) -> {
                UserOrder orderTemp = new UserOrder();
                orderTemp.setOrderId(resultSet.getInt("order_id"));
                orderTemp.setUserId(resultSet.getInt("user_id"));
                orderTemp.setShowId(resultSet.getInt("show_id"));
                orderTemp.setShowSessionId(resultSet.getInt("show_session_id"));
                orderTemp.setStatus(resultSet.getInt("status"));
                orderTemp.setPayment(resultSet.getString("payment"));
                orderTemp.setUserDelete(resultSet.getBoolean("user_delete"));
                orderTemp.setMoney(resultSet.getDouble("money"));
                orderTemp.setTicketCount(resultSet.getInt("ticket_count"));
                try {
                    orderTemp.setTime(format.parse(resultSet.getString("time")));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return orderTemp;
            }, user.getUserId(), status, 0);
        }
        catch (Exception e) {
            Asserts.fail("找不到该订单!");
        }
        return null;
    }

    @Override
    public UserOrder getItem(int id) {
        User user = userService.getCurrentUser();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //已退票的不算
        String sql = "select * from user_order where user_id = ? and order_id = ? and status != 3";
        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                UserOrder orderTemp = new UserOrder();
                orderTemp.setOrderId(resultSet.getInt("order_id"));
                orderTemp.setUserId(resultSet.getInt("user_id"));
                orderTemp.setShowId(resultSet.getInt("show_id"));
                orderTemp.setShowSessionId(resultSet.getInt("show_session_id"));
                orderTemp.setStatus(resultSet.getInt("status"));
                orderTemp.setPayment(resultSet.getString("payment"));
                orderTemp.setUserDelete(resultSet.getBoolean("user_delete"));
                orderTemp.setMoney(resultSet.getDouble("money"));
                orderTemp.setTicketCount(resultSet.getInt("ticket_count"));
                try {
                    orderTemp.setTime(format.parse(resultSet.getString("time")));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return orderTemp;
            }, user.getUserId(), id);
        }
        catch (Exception e) {
            Asserts.fail("找不到该订单!");
        }
        return null;
    }
}
