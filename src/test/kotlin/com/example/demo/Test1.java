package com.example.demo;

import com.example.demo.utils.MySnowFlakeShardingKeyGenerator;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author AlanSun
 * @date 2020/4/29 16:39
 */
public class Test1 {
    @Test
    public void shardingKey() {
        MySnowFlakeShardingKeyGenerator mySnowFlakeShardingKeyGenerator = new MySnowFlakeShardingKeyGenerator();
        System.out.println(Integer.toBinaryString(8191));
        Properties p = new Properties();
        p.setProperty("worker.id", "1");
        mySnowFlakeShardingKeyGenerator.setProperties(p);
        for (int i = 0; i < 8193; i++) {
            final Long comparable = ((Long) mySnowFlakeShardingKeyGenerator.generateKey());
            System.out.println(comparable + " : " + Long.toBinaryString(comparable));
        }
    }
}
