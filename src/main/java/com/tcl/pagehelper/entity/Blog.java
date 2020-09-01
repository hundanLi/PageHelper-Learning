package com.tcl.pagehelper.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author li
 * @version 1.0
 * @date 2020/9/1 23:50
 */
@Data
public class Blog {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
