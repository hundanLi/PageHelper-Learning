package com.tcl.pagehelper.mapper;

import com.github.pagehelper.Page;
import com.tcl.pagehelper.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author li
 * @version 1.0
 * @date 2020/9/1 23:55
 */
@Mapper
public interface BlogMapper {
    Page<Blog> selectList();
}
