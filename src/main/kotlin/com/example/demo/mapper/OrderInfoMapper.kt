package com.example.demo.mapper

import com.example.demo.model.OrderInfo

/**
 * @author AlanSun
 * @date 2020/4/24 14:47
 *
 */
interface OrderInfoMapper {
    fun insert(orderInfo: OrderInfo): Int

//    fun insertUser(orderInfo: OrderInfo): Int
}