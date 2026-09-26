package com.qxkj.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
@EnableAsync //开启异步调用
@EnableScheduling //开启定时任务（阶梯业绩奖励结算等）
@EnableSwagger2
@Configuration
@EnableTransactionManagement
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) //去掉数据源
//@ComponentScan(basePackages={"com.utils",
//        "com.qxkj",
//        "com.exception",
//        "com.common",
//        "com.aop"}) //扫描utils包和父包
//@MapperScan(basePackages = {"com.qxkj.*.dao", "com.qxkj.*.*.dao"})
@ComponentScan(basePackages = {"com.qxkj"})
@MapperScan(basePackages = {"com.qxkj.**.dao"})
public class QianxuAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(QianxuAdminApplication.class, args);
    }

}
