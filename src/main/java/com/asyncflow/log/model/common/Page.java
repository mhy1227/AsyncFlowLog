
package com.asyncflow.log.model.common;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {

    /**
     * 返回数据总数
     */
    private long total;

    /**
     * 封装数据对象
     */
    private List<T> list;

    public Page(List<T> list, long total) {
        this.list=list;
        this.total=total;
    }
}
