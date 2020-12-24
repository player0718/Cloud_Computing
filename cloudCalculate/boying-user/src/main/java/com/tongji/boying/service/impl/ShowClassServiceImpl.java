package com.tongji.boying.service.impl;

import com.github.pagehelper.PageHelper;
import com.tongji.boying.model.ShowClass;
import com.tongji.boying.service.ShowClassService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowClassServiceImpl implements ShowClassService {

    @Override
    public ShowClass detail(Integer classId) {

//        return showClassMapper.selectByPrimaryKey(classId);
        return null;
    }

    @Override
    public List<ShowClass> getShowClassList(int sessionId, Integer pageNum, Integer pageSize) {
//        PageHelper.startPage(pageNum, pageSize);//分页相关
//        ShowClassExample showClassExample = new ShowClassExample();
//        showClassExample.createCriteria().andShowSessionIdEqualTo(sessionId);
//        return showClassMapper.selectByExample(showClassExample);
        return null;
    }
}
