package com.example.demo

import com.example.demo.mapper.OrderInfoMapper
import com.example.demo.model.OrderInfo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    lateinit var orderInfoMapper: OrderInfoMapper

    @Test
    fun contextLoads() {

        for (i in 0..26) {
            val orderInfo = OrderInfo()
            orderInfo.fNo = "${i}"
            orderInfo.userId = i
            orderInfo.createTime = LocalDateTime.now()
            orderInfoMapper.insert(orderInfo)
        }
    }

}
