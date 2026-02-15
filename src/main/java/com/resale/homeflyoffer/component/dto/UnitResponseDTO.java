package com.resale.homeflyoffer.component.dto;

import lombok.Data;

@Data
public class UnitResponseDTO {
    private Integer id;
    private String name;
    private String projectName;
    private String projectCode;
    private String unitModelCode;

    private String usageTypeName;
    private String usageTypeNameAr;
    private String group;
    private String building;
    private String floor;
    private String deliveryDate;
    private String deliveryTextAr;
    private String deliveryText;
    private String area;
}


