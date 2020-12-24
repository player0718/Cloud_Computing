package com.tongji.boying.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BoyingShow implements Serializable {
    @ApiModelProperty(value = " ")
    private Integer showId;

    private String name;

    @ApiModelProperty(value = "所属的目录")
    private Integer categoryId;

    @ApiModelProperty(value = "该演唱会的海报图文信息(url)")
    private String poster;

    @ApiModelProperty(value = "该演唱会的最低价")
    private Double minPrice;

    @ApiModelProperty(value = "该演唱会的最高价")
    private Double maxPrice;

    @ApiModelProperty(value = "该演出展示的优先基本,0为不展示")
    private Integer weight;

    private String city;

    @ApiModelProperty(value = "具体演出地址")
    private String address;

    @ApiModelProperty(value = "演出开始日期")
    private Date dayStart;

    @ApiModelProperty(value = "演出结束日期")
    private Date dayEnd;

    @ApiModelProperty(value = "存储该演唱会等的图文信息")
    private String details;
}
