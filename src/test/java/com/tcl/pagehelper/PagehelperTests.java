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

class PagehelperTests {

    SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() {
        // 1.构建数据源，对应于xml配置中的<dataSource type="POOLED">标签
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");

        // 2.构建Configuration全局配置类实例
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        // <environment id="development">标签
        Environment environment = new Environment("development", transactionFactory, dataSource);
        // <configuration>标签
        Configuration configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        configuration.addMappers("com.tcl.pagehelper.mapper");

        // 添加分页插件！！！！
        configuration.addInterceptor(new PageInterceptor());

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @AfterEach
    void clean() {
    }

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
