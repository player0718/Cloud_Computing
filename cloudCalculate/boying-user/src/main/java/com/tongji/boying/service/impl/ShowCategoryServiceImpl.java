package com.tongji.boying.service.impl;

import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.model.Category;
import com.tongji.boying.service.ShowCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        String sql = "select * from category where weight != 0 and parent_id = ? order by weight desc";
        List<Category> categories = jdbcTemplate.query(sql, (resultSet, i) -> {
            Category category = new Category();
            category.setCategoryId(resultSet.getInt("category_id"));
            category.setParentId(resultSet.getInt("parent_id"));
            category.setName(resultSet.getString("name"));
            category.setWeight(resultSet.getInt("weight"));
            category.setIcon(resultSet.getString("icon"));
            category.setDescription(resultSet.getString("description"));
            return category;
        }, parentId);
        if (categories.size() == 0) {
            Asserts.fail("查询的目录列表为空!");
        }
        return categories;
    }

    @Override
    public Category category(int categoryId) {
        return getCategory(categoryId);
    }

    @Override
    public Category getParentCategory(int categoryId) {
        Category category = getCategory(categoryId);
        if (category.getParentId() == 0) {
            Asserts.fail("已经是父级目录");
        }
        Category parentCategory = getCategory(category.getParentId());
        if (parentCategory == null) {
            Asserts.fail("找不到对应父级目录信息");
        }
        return parentCategory;
    }

    @Override
    public Map<Category, List<Category>> categoryMap() {
        //用LinkedHashMap保持插入顺序,保证最后结果的权重
        Map<Category, List<Category>> map = new LinkedHashMap<>();
        List<Category> parents = categoryList(0);
        for (Category parent : parents) {
            map.put(parent, categoryList(parent.getCategoryId()));
        }
        return map;
    }

    @Override
    public boolean isSonCategory(int categoryId) {
        Category category = getCategory(categoryId);
        if (category.getParentId() == 0) {
            return false;
        }
        return true;
    }

    private Category getCategory(int categoryId) {
        String sql = "select * from category where weight != 0 and category_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                Category category1 = new Category();
                category1.setCategoryId(resultSet.getInt("category_id"));
                category1.setParentId(resultSet.getInt("parent_id"));
                category1.setName(resultSet.getString("name"));
                category1.setWeight(resultSet.getInt("weight"));
                category1.setIcon(resultSet.getString("icon"));
                category1.setDescription(resultSet.getString("description"));
                return category1;
            }, categoryId);
        }
        catch (Exception e) {
            Asserts.fail("要查询的目录不存在");
        }
        return null;
    }
}
