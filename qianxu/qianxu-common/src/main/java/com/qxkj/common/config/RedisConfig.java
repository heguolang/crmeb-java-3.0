package com.qxkj.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPass;

    @Value("${spring.redis.database}")
    private int redisDb;

    @Value("${spring.redis.timeout}")
    private String timeout;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxTotal;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWaitMillis;

    @Value("${spring.redis.jedis.pool.time-between-eviction-runs}")
    private String timeBetweenEvictionRunsMillis;

    @Value("${spring.redis.second.database}")
    private int redisSecondDatabase;

    @Bean(name = "redisConnectionFactory")
    @Primary  //默认选择这个数据源进行执行
    @Qualifier("redisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setPort(redisPort);
        config.setHostName(redisHost);
        config.setDatabase(redisDb);
        config.setPassword(redisPass);
        //获得默认的连接池构造
        //这里需要注意的是，RedisConnectionFactoryJ对于Standalone模式的没有（RedisStandaloneConfiguration，jedisPoolConfig）的构造函数，对此
        //我们用JedisClientConfiguration接口的builder方法实例化一个构造器，还得类型转换
        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder jpConfigBuilder = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration.builder();
        //修改我们的连接池配置
        jpConfigBuilder.usePooling();
        jpConfigBuilder.poolConfig(jedisPoolConfig());
        jpConfigBuilder.readTimeout(Duration.ofMillis(Integer.parseInt(timeout)));
        jpConfigBuilder.connectTimeout(Duration.ofMillis(Integer.parseInt(timeout)));
        //通过构造器来构造jedis客户端配置
        JedisClientConfiguration jedisClientConfiguration = jpConfigBuilder.build();
        //配置连接池属性
        return new JedisConnectionFactory(config, jedisClientConfiguration);
    }

    @Bean(name = "secondRedisConnectionFactory")
    @Qualifier("secondRedisConnectionFactory")
    public RedisConnectionFactory secondRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setPort(redisPort);
        config.setHostName(redisHost);
        config.setDatabase(redisSecondDatabase);
        config.setPassword(redisPass);
        //获得默认的连接池构造
        //这里需要注意的是，RedisConnectionFactoryJ对于Standalone模式的没有（RedisStandaloneConfiguration，jedisPoolConfig）的构造函数，对此
        //我们用JedisClientConfiguration接口的builder方法实例化一个构造器，还得类型转换
        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder jpConfigBuilder = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration.builder();
        //修改我们的连接池配置
        jpConfigBuilder.usePooling();
        jpConfigBuilder.poolConfig(jedisPoolConfig());
        jpConfigBuilder.readTimeout(Duration.ofMillis(Integer.parseInt(timeout)));
        jpConfigBuilder.connectTimeout(Duration.ofMillis(Integer.parseInt(timeout)));
        //通过构造器来构造jedis客户端配置
        JedisClientConfiguration jedisClientConfiguration = jpConfigBuilder.build();
        //配置连接池属性
        return new JedisConnectionFactory(config, jedisClientConfiguration);
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillis));
        return jedisPoolConfig;
    }

}


