package com.tongji.boying.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Category implements Serializable {
    private Integer categoryId;

    @ApiModelProperty(value = "上级分类的编号：0表示一级分类")
    private Integer parentId;

    @ApiModelProperty(value = "目录名称")
    private String name;

    @ApiModelProperty(value = "用于排序,0则不显示")
    private Integer weight;

    @ApiModelProperty(value = "该目录的图标")
    private String icon;

    @ApiModelProperty(value = "目录描述")
    private String description;
}
