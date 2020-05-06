package com.example.demo

import org.mybatis.spring.annotation.MapperScan

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@MapperScan(basePackages = ["com.example.demo.mapper"])
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
