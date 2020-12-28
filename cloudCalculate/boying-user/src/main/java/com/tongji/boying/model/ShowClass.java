package com.tongji.boying.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShowClass implements Serializable {
    private Integer showClassId;

    @ApiModelProperty(value = "所属场次Id")
    private Integer showSessionId;

    @ApiModelProperty(value = "所属哪个级别,例如'学生单日票', '预售单日票', '全价单日票', 'PRO单日票'等等")
    private String name;

    @ApiModelProperty(value = "该级别座位的容量")
    private Integer capacity;

    @ApiModelProperty(value = "该级别座位的定价")
    private Double price;

    @ApiModelProperty(value = "该级别座位的剩余座位数量")
    private Integer remainder;
}
