package com.tongji.boying.service;

import com.tongji.boying.model.ShowClass;

import java.util.List;
import java.util.Map;

public interface ShowClassService
{
    ShowClass detail(Integer classId);

    List<ShowClass> getShowClassList(int sessionId, Integer pageNum, Integer pageSize);

    Map<String, Integer>  ticketCount(Integer classId);
}
