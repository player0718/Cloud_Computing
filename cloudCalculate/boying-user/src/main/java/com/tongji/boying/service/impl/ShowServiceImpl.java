package com.tongji.boying.service.impl;

import cn.hutool.core.util.StrUtil;
import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.model.BoyingShow;
import com.tongji.boying.model.Category;
import com.tongji.boying.service.ShowCategoryService;
import com.tongji.boying.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl implements ShowService {
    @Autowired
    private ShowCategoryService showCategoryService;
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<BoyingShow> search(String keyword, String city, Integer categoryId, Date date, Integer pageNum, Integer pageSize, Integer sort) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sql = new StringBuilder("select * from boying_show where 1 = 1 ");
        //关键词模糊搜索
        if (StrUtil.isNotEmpty(keyword)) {
            sql.append(" and name like '%" + keyword + "%' ");
        }
        //按城市搜索 全国则不限制
        if (StrUtil.isNotEmpty(city) && !city.equals("全国")) {
            sql.append("and city = '" + city + "' ");
        }
        //目录搜索
        if (categoryId != null && categoryId != -1 && categoryId != 0) {
            //说明是子目录,可以直接查
            if (showCategoryService.isSonCategory(categoryId)) {
                sql.append("and category_id = " + categoryId + " ");
            }
            else {
                List<Category> categories = showCategoryService.categoryList(categoryId);

                //show中的分类都是二级分类,所以如果只是直接搜索一级分类的,就需要先把一级分类的所有二级分类先查询
                List<Integer> collect = categories.stream()
                        .map(Category::getCategoryId)
                        .collect(Collectors.toList());
                //一级菜单自己也加上,他本身也算是一个大的分类
                collect.add(categoryId);
                sql.append("and category_id in (");
                for (int i = 0; i < collect.size(); i++) {
                    sql.append(collect.get(i));
                    if (i != collect.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(") ");
            }
        }
       /*
        //按时间搜索
        todo
        if (date != null) {
//            查询的时间在开始时间,结束时间之间
            criteria.andDayStartLessThanOrEqualTo(date).andDayEndGreaterThanOrEqualTo(date);
        }*/

        // TODO: 2020/12/24 分页
        int perId = (pageNum - 1) * pageSize + 1;
        sql.append("and show_id >=" + perId + " limit "+ pageSize);

        //0->按相关度；1->按推荐；2->按时间；3->最低价格从低到高；4->最低价格从高到低
        if (sort == 1) {
            sql.append("sort by weight desc");
        }
        else if (sort == 2) {
            sql.append("sort by day_start desc");
        }
        else if (sort == 3) {
            sql.append("sort by min_price asc");
        }
        else if (sort == 4) {
            sql.append("sort by min_price desc");
        }

        System.out.println(sql.toString());
        List<BoyingShow> boyingShows = null;
        try {
            boyingShows = jdbcTemplate.query(sql.toString(), (resultSet, i) -> {
                BoyingShow boyingShow = new BoyingShow();
                boyingShow.setShowId(resultSet.getInt("show_id"));
                boyingShow.setName(resultSet.getString("name"));
                boyingShow.setCategoryId(resultSet.getInt("category_id"));
                boyingShow.setPoster(resultSet.getString("poster"));
                boyingShow.setDetails(resultSet.getString("details"));
                boyingShow.setMinPrice(resultSet.getDouble("min_price"));
                boyingShow.setMaxPrice(resultSet.getDouble("max_price"));
                boyingShow.setWeight(resultSet.getInt("weight"));
                boyingShow.setCity(resultSet.getString("city"));
                boyingShow.setAddress(resultSet.getString("address"));

                try {
                    boyingShow.setDayStart(format.parse(resultSet.getString("day_start")));
                    boyingShow.setDayEnd(format.parse(resultSet.getString("day_end")));
                    System.out.println(boyingShow.getDayEnd());
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return boyingShow;
            });
        }
        catch (Exception e) {
            Asserts.fail("查询不到信息,参看参数是否有误!");
        }
        return boyingShows;
    }

    @Override
    public BoyingShow detail(int id) {
//        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        String sql = "select * from boying_show where show_id = ?";

        Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql, id);
        System.out.println(stringObjectMap);

        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                BoyingShow boyingShow = new BoyingShow();
                boyingShow.setShowId(resultSet.getInt("show_id"));
                boyingShow.setName(resultSet.getString("name"));
                boyingShow.setCategoryId(resultSet.getInt("category_id"));
                boyingShow.setPoster(resultSet.getString("poster"));
                boyingShow.setDetails(resultSet.getString("details"));
                boyingShow.setMinPrice(resultSet.getDouble("min_price"));
                boyingShow.setMaxPrice(resultSet.getDouble("max_price"));
                boyingShow.setWeight(resultSet.getInt("weight"));
                boyingShow.setCity(resultSet.getString("city"));
                boyingShow.setAddress(resultSet.getString("address"));

                try {
                    boyingShow.setDayStart(format.parse(resultSet.getString("day_start")));
                    boyingShow.setDayEnd(format.parse(resultSet.getString("day_end")));
                    System.out.println(boyingShow.getDayEnd());
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return boyingShow;
            }, id);
        }
        catch (Exception e) {
            Asserts.fail("要查询的演出不存在");
        }
        return null;
    }
}
