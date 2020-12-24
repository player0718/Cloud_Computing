package com.tongji.boying.service.impl;

import com.tongji.boying.model.Category;
import com.tongji.boying.service.ShowCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShowCategoryServiceImpl implements ShowCategoryService {
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Category> categoryList(int parentId) {
//        String sql = "select * from category where weight != 0 and parent_id = ? order by weight desc";
//        List<Category> categories = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class),parentId);
        String sql = "select * from category";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> map : maps) {
            System.out.println(map);
        }
        RowMapper<Category> rowMapper=new BeanPropertyRowMapper<Category>(Category.class);
        List<Category> categories = jdbcTemplate.query(sql, new RowMapper<Category>() {
            @Override
            public Category mapRow(ResultSet resultSet, int i) throws SQLException {
                Category category = new Category();
                category.setCategoryId(resultSet.getInt("category_id"));
                category.setParentId(resultSet.getInt("parent_id"));
                category.setName(resultSet.getString("name"));
                category.setWeight(resultSet.getInt("weight"));
                category.setIcon(resultSet.getString("icon"));
                category.setDescription(resultSet.getString("description"));
                return category;
            }

        });
        for (Category category : categories) {
            System.out.println(category);
        }
        return categories;
    }

    @Override
    public Category category(int categoryId) {
//        CategoryExample categoryExample = new CategoryExample();
//        categoryExample.createCriteria().andWeightNotEqualTo(0).andCategoryIdEqualTo(categoryId);
//        List<Category> categories = categoryMapper.selectByExample(categoryExample);
//        if (categories == null || categories.size() == 0) {
//            Asserts.fail("找不到对应演出目录信息");
//        }
//        return categories.get(0);
        return null;
    }

    @Override
    public Category getParentCategory(int categoryId) {
//        //查询该目录对应的parent
//        Category sonCategory = categoryMapper.selectByPrimaryKey(categoryId);
//        if (sonCategory == null) {
//            Asserts.fail("子菜单不存在");
//        }
//        if (sonCategory.getParentId() == 0) {
//            Asserts.fail("已经是父级菜单");
//        }
//
//        CategoryExample categoryExample = new CategoryExample();
//        categoryExample.createCriteria().andWeightNotEqualTo(0).andCategoryIdEqualTo(sonCategory.getParentId());
//        List<Category> categories = categoryMapper.selectByExample(categoryExample);
//        if (categories == null || categories.size() == 0) {
//            Asserts.fail("找不到对应父级演出目录信息");
//        }

//        return categories.get(0);
        return null;
    }

    @Override
    public Map<Category, List<Category>> categoryMap() {
        //用LinkedHashMap保持插入顺序,保证最后结果的权重
        Map<Category, List<Category>> map = new LinkedHashMap<>();
        List<Category> parents = categoryList(0);
        for (Category parent : parents) {
            map.put(parent, categoryList(parent.getParentId()));
        }
        return map;
    }

    @Override
    public boolean isSonCategory(int id) {
//        CategoryExample categoryExample = new CategoryExample();
//        categoryExample.createCriteria().andCategoryIdEqualTo(id);
//        List<Category> categories = categoryMapper.selectByExample(categoryExample);
//        System.out.println(categories);
//        if (categories.isEmpty()) {
//            //该目录不存在,自然不是子目录
//            return false;
//        }
//        if (categories.get(0).getParentId() == 0 || categories.get(0).getWeight() == 0) {
//            //是父级目录或不能显示,也不是子目录
//            return false;
//        }
        return true;
    }
}
