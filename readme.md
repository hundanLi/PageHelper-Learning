# PageHelper学习笔记

## 1. 快速入门

新建Spring Boot项目

### 1.1 添加依赖

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>5.2.0</version>
</dependency>
```

代码示例中的全部依赖：

```xml
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>5.2.0</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.5</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

pom.xml还需加入以下几行，否则xml映射文件不会被编译到class文件夹中：

```xml
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
            </resource>
        </resources>
	</build>
```



### 1.2 编写实体类和Mapper接口

```java
@Data
public class Blog {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

```

```java
@Mapper
public interface BlogMapper {
    Page<Blog> selectList();
}
```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.tcl.pagehelper.mapper.BlogMapper">

    <select id="selectList" resultType="com.tcl.pagehelper.entity.Blog">
        select * from blog
    </select>

</mapper>
```

### 1.3 编写测试类

数据源配置和SqlSessionFactory都使用Java配置的方式：

```java
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
```



## 2. 框架整合

### 2.1 与Spring Boot整合

#### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.2.13</version>
</dependency>

```

#### 2. 自动配置类

添加这个依赖后，会自动同时引入mybatis-spring-boot-starter等依赖，这样就会自动配置SqlSessionFactory示例，并且通过以下配置类自动为SqlSessionFactory实例加入分页插件。

```java
@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@EnableConfigurationProperties(PageHelperProperties.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class PageHelperAutoConfiguration {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired
    private PageHelperProperties properties;

    /**
     * 接受分页插件额外的属性
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = PageHelperProperties.PAGEHELPER_PREFIX)
    public Properties pageHelperProperties() {
        return new Properties();
    }

    @PostConstruct
    public void addPageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        //先把一般方式配置的属性放进去
        properties.putAll(pageHelperProperties());
        //在把特殊配置放进去，由于close-conn 利用上面方式时，属性名就是 close-conn 而不是 closeConn，所以需要额外的一步
        properties.putAll(this.properties.getProperties());
        interceptor.setProperties(properties);
        // 为注入IOC中的所有SqlSessionFactory实例添加分页插件
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        }
    }

}
```



#### 3. 配置数据源

修改application.yml文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai
    username: root
    password: root
```

#### 4. 编码测试

```java
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
```

