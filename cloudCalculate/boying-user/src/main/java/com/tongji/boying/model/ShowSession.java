package com.tongji.boying.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ShowSession implements Serializable {
    private Integer showSessionId;

    @ApiModelProperty(value = "所属演唱会Id")
    private Integer showId;

    @ApiModelProperty(value = "演出场次开始时间")
    private Date startTime;

    @ApiModelProperty(value = "演出场次结束时间")
    private Date endTime;

    @ApiModelProperty(value = "上映后,已下架等,以及显示的优先级")
    private Integer weight;
}
