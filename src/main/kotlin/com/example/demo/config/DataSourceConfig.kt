package com.example.demo.config

import com.zaxxer.hikari.HikariDataSource
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.sql.DataSource

/**
 * @author AlanSun
 * @date 2020/4/24 14:45
 *
 */
@Configuration
class DataSourceConfig {
    @Bean
    fun dataSource(): DataSource {
        // 配置真实数据源
        val dataSourceMap: MutableMap<String, DataSource> = HashMap()

        // 配置第一个数据源
        val dataSource1 = HikariDataSource()
        dataSource1.driverClassName = "com.mysql.jdbc.Driver"
        dataSource1.jdbcUrl = "jdbc:mysql://192.168.34.132:3306/test"
        dataSource1.username = "root"
        dataSource1.password = "123456"
        dataSourceMap["ds0"] = dataSource1

        // 配置Order 表规则
        val orderTableRuleConfig = TableRuleConfiguration("order_info", "ds0.order_info\${0..2}")

        // 配置分库 + 分表策略
//        orderTableRuleConfig.databaseShardingStrategyConfig = InlineShardingStrategyConfiguration("user_id", "ds\${user_id % 2}")
        orderTableRuleConfig.tableShardingStrategyConfig = InlineShardingStrategyConfiguration("f_id", "order_info\${(f_id & (8191)) % 3}")
        // 主键生成方案
        val p = Properties()
        p.setProperty("worker.id", "1")
        orderTableRuleConfig.keyGeneratorConfig = KeyGeneratorConfiguration("MY-SNOWFLAKE", "f_id", p)

        // 配置分片规则
        val shardingRuleConfig = ShardingRuleConfiguration()
        shardingRuleConfig.tableRuleConfigs.add(orderTableRuleConfig)

        // 省略配置order_item表规则...
        // ...

        // 获取数据源对象
        val dataSource: DataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, Properties())
        return dataSource
    }
}