package com.tongji.boying.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserOrder implements Serializable {
    private Integer orderId;

    @ApiModelProperty(value = "所属用户Id")
    private Integer userId;

    @ApiModelProperty(value = "所属演出Id")
    private Integer showId;

    @ApiModelProperty(value = "所属场次Id")
    private Integer showSessionId;

    @ApiModelProperty(value = "这些票要邮寄到什么地方")
    private Integer addressId;

    @ApiModelProperty(value = "这些票的实际观演人")
    private Integer frequentId;

    @ApiModelProperty(value = "待评价,已完成,已退订单(1,2,3)")
    private Integer status;

    @ApiModelProperty(value = "订单提交时间")
    private Date time;

    @ApiModelProperty(value = "订单支付方式")
    private String payment;

    @ApiModelProperty(value = "该订单对用户是否可见,即用户是否删除了该订单")
    private Boolean userDelete;

    @ApiModelProperty(value = "订单总金额")
    private Double money;

    @ApiModelProperty(value = "票的总数")
    private Integer ticketCount;
}
