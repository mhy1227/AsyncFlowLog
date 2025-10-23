/*
 * Copyright (C) 2017-2021
 * All rights reserved, Designed By 深圳中科鑫智科技有限公司
 * Copyright authorization contact 18814114118
 */
package com.asyncflow.log.model.common;

import lombok.Data;

/**
 * 分页通用实体
 */
@Data
public class PageParam {

    /**
     * 当前页
     */
    private Integer page = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    private String isAsc;

    public void setIsAsc(String isAsc) {
        if("desc".equals(isAsc) || "asc".equals(isAsc)){
            this.isAsc = isAsc;
        }else{
            this.isAsc = "desc";
        }
    }
}
