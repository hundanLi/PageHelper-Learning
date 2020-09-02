package com.tcl.pagehelper;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageInterceptor;
import com.tcl.pagehelper.entity.Blog;
import com.tcl.pagehelper.mapper.BlogMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PagehelperBootTests {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Test
    void getStarted() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
            // 调用分页方法后只有紧接着的查询操作才有效
            PageHelper.startPage(2, 5);
            Page<Blog> blogs = mapper.selectList();
            PageInfo<Blog> pageInfo = new PageInfo<>(blogs);
            System.err.println(pageInfo);
        }

    }

}
