package com.tongji.boying.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Ticket implements Serializable {
    private Integer ticketId;

    @ApiModelProperty(value = "所属订单Id")
    private Integer orderId;

    @ApiModelProperty(value = "所属座次Id")
    private Integer showClassId;

    @ApiModelProperty(value = "二维码图片,供观影人验证入场")
    private String qrCodeUrl;

}
